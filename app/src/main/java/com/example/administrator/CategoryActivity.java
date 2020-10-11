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
import android.view.MenuItem;
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
import io.grpc.CallCredentials2;

import static com.example.administrator.SetsActivity.selected_set_index;
import static com.example.administrator.SetsActivity.setList;

public class CategoryActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.cat_recycler)
    RecyclerView cat_recycler_view;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.addCatB)
    Button addCatBut;
    private Dialog loadingDialog;
    private Dialog addCatDialog;
    @Nullable
    private EditText dialogCatName;
    @Nullable
    private Button dialogAddBut;
    public static List<CategoryModel>catList = new ArrayList<>();
    public static int selected_cat_index = 0;
    private FirebaseFirestore firestore;
    private CategoryAdapter categoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Categories");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        cat_recycler_view.setLayoutManager(layoutManager);


        loadingDialog = new Dialog(CategoryActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);



        addCatDialog = new Dialog(CategoryActivity.this);
        addCatDialog.setContentView(R.layout.add_set_dialogue);
        addCatDialog.setCancelable(true);
        addCatDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        dialogCatName = addCatDialog.findViewById(R.id.ad_cat_name);
        dialogAddBut = addCatDialog.findViewById(R.id.ac_add_btn);

        firestore = FirebaseFirestore.getInstance();

        loadData();

        addCatBut.setOnClickListener(this);
        dialogAddBut.setOnClickListener(this);
    }



    @Override
    public void onClick(View view) {

        if (view == addCatBut){
            dialogCatName.getText().clear();
            addCatDialog.show();

        }
        if (view == dialogAddBut){
            if (dialogCatName.getText().toString().isEmpty()){
                dialogCatName.setError("Enter Category Name");
                return;
            }
            addnewCategory(dialogCatName.getText().toString());

        }
    }



    private void loadData() {
        catList.clear();
        loadingDialog.show();

        String curr_set_id = setList.get(selected_set_index).getId();

        firestore.collection("DETOUR").document(curr_set_id)
                .collection("CAT").document("categories")
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()){
                        long nbrofcat = (long) documentSnapshot.get("COUNT1");
                        for (int i = 1; i <= nbrofcat; i++){
                            String catName = documentSnapshot.getString("CAT" + String.valueOf(i) +"_NAME");
                            String catid = documentSnapshot.getString("CAT" + String.valueOf(i) + "_ID");
                            catList.add(new CategoryModel(catid,catName));
                            if (i==3){
                                addCatBut.setVisibility(View.GONE);
                            }
                        }

                        categoryAdapter = new CategoryAdapter(catList);
                        cat_recycler_view.setAdapter(categoryAdapter);
                    }

                    else {
                        Toast.makeText(CategoryActivity.this, "No Category Document Exists", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                    Toast.makeText(CategoryActivity.this,task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
                loadingDialog.dismiss();
            }
        });

    }


    private void addnewCategory(String catName) {
        addCatDialog.dismiss();
        loadingDialog.show();

        String curr_set_id = setList.get(selected_set_index).getId();


        Map<String,Object> mdata = new ArrayMap<>();
        mdata.put("COUNT2",0);




        String set_id = firestore.collection("DETOUR").document().getId();
        String cate_id = firestore.collection("DETOUR").document(set_id).collection("CAT").document().getId();

        Map<String,Object> namecat = new ArrayMap<>();
        namecat.put("CatName",catName);

        firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document(cate_id)
                .set(namecat);


        firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document(cate_id).collection("SubCat").document("M_List")
                .set(mdata)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {


                        Map<String,Object> catDoc = new ArrayMap<>();
                        catDoc.put("CAT" + String.valueOf(catList.size() + 1)+ "_NAME",catName);
                        catDoc.put("CAT" + String.valueOf(catList.size() + 1)+ "_ID",cate_id);
                        catDoc.put("COUNT1",catList.size() + 1);



                        firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document("categories")
                                .update(catDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(CategoryActivity.this, "Category Added SuccessFully", Toast.LENGTH_SHORT).show();
                                        catList.add(new CategoryModel(cate_id,catName));

                                        categoryAdapter.notifyItemInserted(catList.size());

                                        loadingDialog.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                loadingDialog.dismiss();
                                Toast.makeText(CategoryActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                                Toast.makeText(CategoryActivity.this, "ttttt", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(CategoryActivity.this,e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}