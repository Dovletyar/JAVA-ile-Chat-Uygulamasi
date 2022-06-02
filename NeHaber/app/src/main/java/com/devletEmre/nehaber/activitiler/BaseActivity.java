package com.devletEmre.nehaber.activitiler;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.devletEmre.nehaber.utilities.Sabitler;
import com.devletEmre.nehaber.utilities.TercihYoneticisi;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference documentReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TercihYoneticisi tercihYoneticisi = new TercihYoneticisi(getApplicationContext());
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        documentReference=database.collection(Sabitler.KEY_COLLECTION_USERS)
                .document(tercihYoneticisi.getString(Sabitler.KEY_USER_ID));
    }

    @Override
    protected void onPause() {
        super.onPause();
        documentReference.update(Sabitler.KEY_AVAILABILITY,0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        documentReference.update(Sabitler.KEY_AVAILABILITY,1);
    }
}
