package com.devletEmre.nehaber.activitiler;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.devletEmre.nehaber.adapterler.UsersAdapter;
import com.devletEmre.nehaber.databinding.ActivityUsersBinding;
import com.devletEmre.nehaber.listenerler.UserListener;
import com.devletEmre.nehaber.modeller.User;
import com.devletEmre.nehaber.utilities.Sabitler;
import com.devletEmre.nehaber.utilities.TercihYoneticisi;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private TercihYoneticisi tercihYoneticisi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tercihYoneticisi = new TercihYoneticisi(getApplicationContext());
        setListeners();
        getUsers();

    }


    private void setListeners(){
        binding.resimGeri.setOnClickListener(v->onBackPressed());
    }


    private void getUsers(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Sabitler.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task->{
                    loading(false);
                    String currentUserId = tercihYoneticisi.getString(Sabitler.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() !=null) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Sabitler.KEY_NAME);
                            user.email = queryDocumentSnapshot.getString(Sabitler.KEY_EMAIL);
                            user.image = queryDocumentSnapshot.getString(Sabitler.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Sabitler.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            UsersAdapter userAdapter = new UsersAdapter(users, this);
                            binding.kullaniciGeriDonusum.setAdapter(userAdapter);
                            binding.kullaniciGeriDonusum.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    }
                    else{
                        showErrorMessage();

                    }
                });
    }

    private void showErrorMessage(){
        binding.txtHataMesaji.setText(String.format("%s","Erişilebilir kullanıcı yok"));
        binding.txtHataMesaji.setVisibility(View.VISIBLE);
    }


    private void loading(Boolean isLoading){
        if (isLoading){
            binding.ilerlemeCubugu.setVisibility(View.VISIBLE);
        }
        else{
            binding.ilerlemeCubugu.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onUserClicked(User user) {
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Sabitler.KEY_USER,user);
        startActivity(intent);
        finish();
    }
}