package com.example.administrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.administrator.CategoryActivity.catList;
import static com.example.administrator.CategoryActivity.selected_cat_index;
import static com.example.administrator.SetsActivity.selected_set_index;
import static com.example.administrator.SetsActivity.setList;
import static com.example.administrator.SubCategoryActivity.level_list;
import static com.example.administrator.SubCategoryActivity.selected_money_index;

public class QuestionListActivity extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.q_recycler)
    RecyclerView quesView;
    @BindView(R.id.addQuestionB)
    Button addQB;
    public static List<QuestionModel> quesList = new ArrayList<>();
    private QuestionAdapter questionAdapter;
    private FirebaseFirestore firestore;
    private Dialog loadingDialog;
    public Dialog mDialog;
    public Button mDialogyes,mDialogno;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
        ButterKnife.bind(this);

        createDialog();

        Toolbar toolbar = findViewById(R.id.q_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Questions");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        loadingDialog = new Dialog(QuestionListActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        addQB.setOnClickListener(this);

        firestore = FirebaseFirestore.getInstance();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        quesView.setLayoutManager(layoutManager);

        loadQuestions();
    }




    //        String curr_set_id = setList.get(selected_set_index).getId();
//        String curr_cat_id = catList.get(selected_cat_index).getId();
//        String curr_sub_id = level_list.get(selected_money_index).getId();
//        quesList.clear();
    @Override
    public void onClick(View view) {
        if (view == addQB){
            Intent intent = new Intent(QuestionListActivity.this,QuestionDetailsActivity.class);
            intent.putExtra("ACTION","ADD");
            startActivity(intent);
            finish();
        }
    }


    private void loadQuestions() {
        quesList.clear();

        loadingDialog.show();

        firestore.collection("DETOUR").document(setList.get(selected_set_index).getId())
                .collection("CAT").document(catList.get(selected_cat_index).getId())
                .collection("SubCat").document(level_list.get(selected_money_index).getId())
                .collection("QUESTION").get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();

                        for (QueryDocumentSnapshot doc: queryDocumentSnapshots){
                            docList.put(doc.getId(),doc);
                        }

                        QueryDocumentSnapshot quesListDoc = docList.get("qList");

                        String count =quesListDoc.getString("COUNT");

                        for (int i=0;i < Integer.valueOf(count);i++){

                            String quesID = quesListDoc.getString("Q" + String.valueOf(i+1) + "_ID");

                            QueryDocumentSnapshot quesDoc = docList.get(quesID);

                            quesList.add(new QuestionModel(
                                    quesID,
                                    quesDoc.getString("QUESTION"),
                                    quesDoc.getString("A"),
                                    quesDoc.getString("B"),
                                    quesDoc.getString("C"),
                                    quesDoc.getString("ANSWER")

                            ));
                            if (Integer.valueOf(count) == 1){
                                addQB.setVisibility(View.GONE);
                            }
                            else {
                                addQB.setVisibility(View.VISIBLE);
                            }
                        }
                        questionAdapter = new QuestionAdapter(quesList);
                        quesView.setAdapter(questionAdapter);


                        loadingDialog.dismiss();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionListActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(questionAdapter != null) {
            questionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == android.R.id.home)
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    protected void createDialog() {
        mDialog = new Dialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.dialog_exist);

    }
}