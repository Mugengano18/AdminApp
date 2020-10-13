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

import static com.example.administrator.CategoryActivity.catList;
import static com.example.administrator.CategoryActivity.selected_cat_index;
import static com.example.administrator.SetsActivity.selected_set_index;
import static com.example.administrator.SetsActivity.setList;

public class SubCategoryActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.sub_recycler)
    RecyclerView sub_recycler_view;
    @BindView(R.id.sub_toolbar)
    Toolbar sub_toolbar;
    @BindView(R.id.addSubB)
    Button addSubBut;
    private Dialog loadingDialog;
    private Dialog addCatDialog;
    @Nullable
    private EditText dialogMoneyName;
    @Nullable
    private Button dialogAddBut;
    public static List<SubCatModel> level_list = new ArrayList<>();
    public static int selected_money_index = 0;
    private FirebaseFirestore firestore;
    private SubCatAdapter subCatAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);
        ButterKnife.bind(this);
        setSupportActionBar(sub_toolbar);
        getSupportActionBar().setTitle("Levels");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        sub_recycler_view.setLayoutManager(layoutManager);


        loadingDialog = new Dialog(SubCategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);



        addCatDialog = new Dialog(SubCategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_set_dialogue);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogMoneyName = addCatDialog.findViewById(R.id.ad_cat_name);
        dialogAddBut = addCatDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        loadData();

        addSubBut.setOnClickListener(this);
        dialogAddBut.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {
        if (view == addSubBut){
            dialogMoneyName.getText().clear();
            addCatDialog.show();

        }
        if (view == dialogAddBut){
            if (dialogMoneyName.getText().toString().isEmpty()){
                dialogMoneyName.setError("Enter Category Name");
                return;
            }
            addnewSubCat(Long.valueOf(Integer.valueOf(dialogMoneyName.getText().toString())));

        }
    }




    private void loadData() {
        level_list.clear();
        loadingDialog.show();

        String curr_set_id = setList.get(selected_set_index).getId();
        String curr_cat_id = catList.get(selected_cat_index).getId();

        firestore.collection("DETOUR").document(curr_set_id)
                .collection("CAT").document(curr_cat_id)
                .collection("SubCat").document("M_List")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        long nbroflevels = (long) documentSnapshot.get("COUNT2");
                        for (int i = 1;i <= nbroflevels;i++){
                            long lname = (long) documentSnapshot.get("M" + String.valueOf(i) + "_NAME");
                            String lid = documentSnapshot.getString("M" + String.valueOf(i) + "_ID");
                            level_list.add(new SubCatModel(lid,lname));
                            if (lname == 3){
                                addSubBut.setVisibility(View.GONE);
                            }
                            else {
                                addSubBut.setVisibility(View.VISIBLE);
                            }
                        }
                        subCatAdapter = new SubCatAdapter(level_list);
                        sub_recycler_view.setAdapter(subCatAdapter);
                    }
                    else {
                        Toast.makeText(SubCategoryActivity.this, "No levels Available", Toast.LENGTH_SHORT).show();
                    }

                }
                else {
                    Toast.makeText(SubCategoryActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });
    }

    private void addnewSubCat(Long Money) {
        addCatDialog.dismiss();
        loadingDialog.show();

        String curr_set_id = setList.get(selected_set_index).getId();
        String curr_cat_id = catList.get(selected_cat_index).getId();


        Map<String,Object> qdata = new ArrayMap<>();
        qdata.put("COUNT",0);



        String set_id = firestore.collection("DETOUR").document().getId();
        String cate_id = firestore.collection("DETOUR").document(set_id).collection("CAT").document().getId();
        String M_id = firestore.collection("DETOUR").document(set_id).collection("CAT").document(cate_id).collection("SubCat").document().getId();

        Map<String, Long> Namel= new ArrayMap<>();
        Namel.put("Level",Money);

        firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document(curr_cat_id).collection("SubCat").document(M_id)
                .set(Namel);



        firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document(curr_cat_id)
                .collection("SubCat").document(M_id).collection("QUESTION").document("qList")
                .set(qdata)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> mDoc = new ArrayMap<>();
                        mDoc.put("M" + String.valueOf(level_list.size() + 1) + "_NAME",Money);
                        mDoc.put("M" + String.valueOf(level_list.size() + 1) + "_ID",M_id);
                        mDoc.put("COUNT2",level_list.size() + 1);


                        firestore.collection("DETOUR").document(curr_set_id).collection("CAT")
                                .document(curr_cat_id).collection("SubCat").document("M_List")
                                .update(mDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SubCategoryActivity.this, "Level Added SuccessFully", Toast.LENGTH_SHORT).show();
                                        level_list.add(new SubCatModel(M_id,Money));
                                        subCatAdapter.notifyItemInserted(level_list.size());

                                        loadingDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                                loadingDialog.dismiss();
                                Toast.makeText(SubCategoryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                                Toast.makeText(SubCategoryActivity.this, "MMMMMM", Toast.LENGTH_SHORT).show();
                            }
                        });
                     }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                loadingDialog.dismiss();
                Toast.makeText(SubCategoryActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        });
    }

}