package com.devletEmre.nehaber.activitiler;

import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.devletEmre.nehaber.databinding.ActivityGirisYapBinding;
import com.devletEmre.nehaber.utilities.Sabitler;
import com.devletEmre.nehaber.utilities.TercihYoneticisi;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.regex.Pattern;

public class GirisYapActivity extends AppCompatActivity {

    private ActivityGirisYapBinding binding;
    private TercihYoneticisi tercihYoneticisi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //eger giris yapılmıs ise direk ana menuye geçecek
        tercihYoneticisi = new TercihYoneticisi(getApplicationContext());
        if(tercihYoneticisi.getBoolean(Sabitler.KEY_IS_SIGNED_IN)){
            Intent intent = new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivityGirisYapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners(){
        binding.txtYeniHesapOlustur.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), KayitOlActivity.class)));
        binding.btnGirisYap.setOnClickListener(v->{
            if (isValidSignInDetails()){
                signIn();
            }
        });


    }

    private void signIn(){
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Sabitler.KEY_COLLECTION_USERS)
                .whereEqualTo(Sabitler.KEY_EMAIL,binding.girisMail.getText().toString())
                .whereEqualTo(Sabitler.KEY_PASSWORD,binding.girisSifre.getText().toString())
                .get()
                .addOnCompleteListener(task->{
                   if (task.isSuccessful() && task.getResult() != null & task.getResult().getDocuments().size()>0){
                       DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                       tercihYoneticisi.putBoolean(Sabitler.KEY_IS_SIGNED_IN,true);
                       tercihYoneticisi.putString(Sabitler.KEY_USER_ID,documentSnapshot.getId());
                       tercihYoneticisi.putString(Sabitler.KEY_NAME,documentSnapshot.getString(Sabitler.KEY_NAME));
                       tercihYoneticisi.putString(Sabitler.KEY_IMAGE,documentSnapshot.getString(Sabitler.KEY_IMAGE));
                       Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                       intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                       startActivity(intent);
                   }
                   else{
                       loading(false);
                       showToast("Email adresi veya şifre yanlış");
                   }
                });
    }

    private void loading(Boolean isLoading){
        if (isLoading){
            binding.btnGirisYap.setVisibility(View.INVISIBLE);
            binding.ilerlemeCubugu.setVisibility(View.VISIBLE);
        }
        else{
            binding.ilerlemeCubugu.setVisibility(View.INVISIBLE);
            binding.btnGirisYap.setVisibility(View.VISIBLE);
        }
    }



    private void showToast(String  message){
        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
    }

    private Boolean isValidSignInDetails(){
        if(binding.girisMail.getText().toString().trim().isEmpty()){
            showToast("Email adresinizi giriniz");
            return  false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.girisMail.getText().toString()).matches()){
            showToast("Lütfen geçerli bir mail adresi giriniz");
            return false;
        }
        else if (binding.girisSifre.getText().toString().trim().isEmpty()){
            showToast("Şifrenizi giriniz");
            return false;
        }
        else{
            return true;
        }

    }



}