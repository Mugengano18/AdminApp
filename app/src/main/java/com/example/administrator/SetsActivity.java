package com.example.administrator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SetsActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.set_recycler)
    RecyclerView setsView;
    @BindView(R.id.addSetB)
    Button addSetB;
    private Dialog addCatDialog;
    @Nullable
    private EditText dialogCatName;
    @Nullable
    private Button dialogAddBut;

    private SetAdapter adapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;
    @BindView(R.id.set_toolbar)
    Toolbar toolbar;
    public static List<SetModel>setList = new ArrayList<>();

    public static int selected_set_index = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);
        ButterKnife.bind(this);


        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Sets");


        loadingDialog = new Dialog(SetsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        addCatDialog = new Dialog(SetsActivity.this);
        addCatDialog.setContentView(R.layout.add_set_dialogue);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);



        dialogCatName = addCatDialog.findViewById(R.id.ad_cat_name);
        dialogAddBut = addCatDialog.findViewById(R.id.ac_add_btn);


        firestore = FirebaseFirestore.getInstance();


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        setsView.setLayoutManager(layoutManager);

        loadSets();
        addSetB.setText("ADD NEW SET");
        addSetB.setOnClickListener(this);
        dialogAddBut.setOnClickListener(this);
    }




    @Override
    public void onClick(View view) {

        if (view == addSetB){
            dialogCatName.getText().clear();
            addCatDialog.show();

        }
        if (view == dialogAddBut){
            if (dialogCatName.getText().toString().isEmpty()){
                dialogCatName.setError("Enter Category Name");
                return;
            }
            addNewSet(dialogCatName.getText().toString());

        }
    }




    private void loadSets() {
        loadingDialog.show();
        setList.clear();

        firestore.collection("DETOUR").document("SETS")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        long count = (long) documentSnapshot.get("COUNT");
                        for (int i=1; i<= count; i++){
                            String setName = documentSnapshot.getString("SET" + String.valueOf(i) + "_NAME");
                            String setid = documentSnapshot.getString("SET" + String.valueOf(i) + "_ID");
                            setList.add(new SetModel(setid,setName));
                            if (count == 3){
                                addSetB.setVisibility(View.GONE);
                            }
                            else {
                                addSetB.setVisibility(View.VISIBLE);
                            }
                        }

                        adapter = new SetAdapter(setList);
                        setsView.setAdapter(adapter);
                    }
                    else {
                        Toast.makeText(SetsActivity.this, "No Set Document Exists", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    Toast.makeText(SetsActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });
    }



    private void addNewSet(String title) {

        addCatDialog.dismiss();
        loadingDialog.show();

        Map<String,Object> catData = new ArrayMap<>();
        catData.put("COUNT1",0);

        Map<String,Object>fields = new ArrayMap<>();
        fields.put("NAME",title);


        String set_id = firestore.collection("DETOUR").document().getId();

        firestore.collection("DETOUR").document(set_id)
                .set(fields);


        firestore.collection("DETOUR").document(set_id).collection("CAT").document("categories")
                .set(catData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("SET" + String.valueOf(setList.size() + 1)+ "_NAME",title);
                        catDoc.put("SET" + String.valueOf(setList.size() + 1)+ "_ID",set_id);
                        catDoc.put("COUNT",setList.size() + 1);



                        firestore.collection("DETOUR").document("SETS")
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SetsActivity.this, "SET Added SuccessFully", Toast.LENGTH_SHORT).show();
                                        setList.add(new SetModel(set_id,title));

                                        adapter.notifyItemInserted(setList.size());

                                        loadingDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismiss();
                                Toast.makeText(SetsActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(SetsActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });
    }






















































}