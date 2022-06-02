package com.devletEmre.nehaber.activitiler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.devletEmre.nehaber.adapterler.RecentConversationsAdapter;
import com.devletEmre.nehaber.databinding.ActivityMainBinding;
import com.devletEmre.nehaber.listenerler.ConversionListener;
import com.devletEmre.nehaber.modeller.ChatMessage;
import com.devletEmre.nehaber.modeller.User;
import com.devletEmre.nehaber.utilities.Sabitler;
import com.devletEmre.nehaber.utilities.TercihYoneticisi;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends BaseActivity implements ConversionListener {

    private ActivityMainBinding binding;
    private TercihYoneticisi tercihYoneticisi;
    private List<ChatMessage> conversations;
    private RecentConversationsAdapter conversationsAdapter;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tercihYoneticisi = new TercihYoneticisi(getApplicationContext());
        init();
        loadUserDetails();
        getToken();
        setListener();
        listenConversations();
    }

    private void init(){
        conversations=new ArrayList<>();
        conversationsAdapter=new RecentConversationsAdapter(conversations, this);
        binding.conversationsRecyclerView.setAdapter(conversationsAdapter);
        database=FirebaseFirestore.getInstance();
    }

    private void setListener(){
        binding.ResimSignOut.setOnClickListener(v->signOut());
        binding.fabYeniChat.setOnClickListener(v->
                startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
    }


    private void loadUserDetails(){
        binding.txtAd.setText(tercihYoneticisi.getString(Sabitler.KEY_NAME));
        byte[] bytes = Base64.decode(tercihYoneticisi.getString(Sabitler.KEY_IMAGE), Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.ProfilResmi.setImageBitmap(bitmap);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private void listenConversations(){
        database.collection(Sabitler.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Sabitler.KEY_SENDER_ID,tercihYoneticisi.getString(Sabitler.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Sabitler.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Sabitler.KEY_RECEIVER_ID,tercihYoneticisi.getString(Sabitler.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error)->{
        if (error != null){
            return;
        }
        if (value !=null){
            for (DocumentChange documentChange : value.getDocumentChanges()){
                if (documentChange.getType()== DocumentChange.Type.ADDED){
                    String senderId = documentChange.getDocument().getString(Sabitler.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Sabitler.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId=senderId;
                    chatMessage.receiverId=receiverId;
                    if (tercihYoneticisi.getString(Sabitler.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage=documentChange.getDocument().getString(Sabitler.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Sabitler.KEY_RECEIVER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Sabitler.KEY_RECEIVER_ID);
                    }
                    else{
                        chatMessage.conversionImage=documentChange.getDocument().getString(Sabitler.KEY_SENDER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Sabitler.KEY_SENDER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Sabitler.KEY_SENDER_ID);
                    }
                    chatMessage.message=documentChange.getDocument().getString(Sabitler.KEY_LAST_MESSAGE);
                    chatMessage.dateObject=documentChange.getDocument().getDate(Sabitler.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                }
                else if(documentChange.getType()==DocumentChange.Type.MODIFIED){
                    for (int i=0; i<conversations.size(); i++){
                        String senderId = documentChange.getDocument().getString(Sabitler.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Sabitler.KEY_RECEIVER_ID);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)){
                            conversations.get(i).message=documentChange.getDocument().getString(Sabitler.KEY_LAST_MESSAGE);
                            conversations.get(i).dateObject=documentChange.getDocument().getDate(Sabitler.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1, obj2) -> obj2.dateObject.compareTo(obj1.dateObject));
            conversationsAdapter.notifyDataSetChanged();
            binding.conversationsRecyclerView.smoothScrollToPosition(0);
            binding.conversationsRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }


    private void updateToken(String token){
        tercihYoneticisi.putString(Sabitler.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Sabitler.KEY_COLLECTION_USERS).document(
                        tercihYoneticisi.getString(Sabitler.KEY_USER_ID)
                );
        documentReference.update(Sabitler.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> showToast("Token güncellenemiyor"));
    }

    private void signOut(){
        showToast("Çıkış yapılıyor...");
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Sabitler.KEY_COLLECTION_USERS).document(
                        tercihYoneticisi.getString(Sabitler.KEY_USER_ID)
                );
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Sabitler.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    tercihYoneticisi.clear();
                    startActivity(new Intent(getApplicationContext(), GirisYapActivity.class));
                })
                .addOnFailureListener(e -> showToast("Çıkış yapılamıyor"));
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Sabitler.KEY_USER,user);
        startActivity(intent);
    }
}