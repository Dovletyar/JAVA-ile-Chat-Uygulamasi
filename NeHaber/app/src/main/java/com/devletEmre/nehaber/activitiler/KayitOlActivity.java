package com.devletEmre.nehaber.activitiler;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.devletEmre.nehaber.databinding.ActivityKayitOlBinding;
import com.devletEmre.nehaber.utilities.Sabitler;
import com.devletEmre.nehaber.utilities.TercihYoneticisi;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class KayitOlActivity extends AppCompatActivity {

    private ActivityKayitOlBinding binding;
    private TercihYoneticisi tercihYoneticisi;
    private String encodedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityKayitOlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tercihYoneticisi = new TercihYoneticisi(getApplicationContext());
        setListeners();
    }


    private void setListeners() {
        binding.txtGirisYap.setOnClickListener(view -> onBackPressed());
        binding.btnUyeOl.setOnClickListener(view -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });
        binding.layoutResim.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Sabitler.KEY_NAME, binding.girisAd.getText().toString());
        user.put(Sabitler.KEY_EMAIL, binding.girisMail.getText().toString());
        user.put(Sabitler.KEY_PASSWORD, binding.girisSifre.getText().toString());
        user.put(Sabitler.KEY_IMAGE, encodedImage);
        database.collection(Sabitler.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    tercihYoneticisi.putBoolean(Sabitler.KEY_IS_SIGNED_IN, true);
                    tercihYoneticisi.putString(Sabitler.KEY_USER_ID, documentReference.getId());
                    tercihYoneticisi.putString(Sabitler.KEY_NAME, binding.girisAd.getText().toString());
                    tercihYoneticisi.putString(Sabitler.KEY_IMAGE, encodedImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);

    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.resimProfil.setImageBitmap(bitmap);
                            binding.txtResimEkle.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Profil resminizi ekleyiniz");
            return false;
        } else if (binding.girisAd.getText().toString().trim().isEmpty()) {
            showToast("İsminizi giriniz");
            return false;
        } else if (binding.girisMail.getText().toString().trim().isEmpty()) {
            showToast("Email adresinizi giriniz");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.girisMail.getText().toString()).matches()) {
            showToast("Lütfen geçerli bir mail adresi giriniz");
            return false;
        } else if (binding.girisSifre.getText().toString().trim().isEmpty()) {
            showToast("Şifrenizi giriniz");
            return false;
        } else if (binding.girisSifreTekrar.getText().toString().trim().isEmpty()) {
            showToast("Şifrenizi tekrar giriniz");
            return false;
        } else if (!binding.girisSifre.getText().toString().equals(binding.girisSifreTekrar.getText().toString())) {
            showToast("Şifreler eşleşmiyor!");
            return false;
        } else {
            return true;
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.btnUyeOl.setVisibility(View.INVISIBLE);
            binding.ilerlemeCubugu.setVisibility(View.VISIBLE);
        } else {
            binding.ilerlemeCubugu.setVisibility(View.INVISIBLE);
            binding.btnUyeOl.setVisibility(View.VISIBLE);
        }
    }


}