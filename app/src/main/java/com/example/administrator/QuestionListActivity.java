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
import android.view.View;
import android.view.ViewGroup;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_list);
        ButterKnife.bind(this);


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




    @Override
    public void onClick(View view) {
        if (view == addQB){
            Intent intent = new Intent(QuestionListActivity.this,QuestionDetailsActivity.class);
            intent.putExtra("ACTION","ADD");
            startActivity(intent);
        }
    }

//    private void loadQuestions() {
//
//        String curr_set_id = setList.get(selected_set_index).getId();
//        String curr_cat_id = catList.get(selected_cat_index).getId();
//        String curr_sub_id = level_list.get(selected_money_index).getId();
//        quesList.clear();
//
//        loadingDialog.show();
//
//        firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document(curr_cat_id)
//                .collection("SubCat").document(curr_sub_id).collection("QUESTION")
//                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//            @Override
//            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                Map<String, QueryDocumentSnapshot> docList = new ArrayMap<>();
//
//                for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots){
//                    docList.put(documentSnapshot.getId(),documentSnapshot);
//                }
//                QueryDocumentSnapshot quesListDoc = docList.get("qList");
//
//                long count = (long) quesListDoc.get("COUNT");
//
//                for (int i = 0;i <= count;i++){
//                    String quesID = quesListDoc.getString("Q" +String.valueOf(i) + "_ID");
//                    QueryDocumentSnapshot quesDoc = docList.get(quesID);
//
//                    quesList.add(new QuestionModel(
//                            quesID,
//                            quesDoc.getString("QUESTION"),
//                            quesDoc.getString("A"),
//                            quesDoc.getString("B"),
//                            quesDoc.getString("C"),
//                           Integer.valueOf(quesDoc.getString("ANSWER"))
//                    ));
//                }
//
//                questionAdapter = new QuestionAdapter(quesList);
//                quesView.setAdapter(questionAdapter);
//
//                loadingDialog.dismiss();
//
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Toast.makeText(QuestionListActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
//                loadingDialog.dismiss();
//            }
//        });
//
//    }

    private void loadQuestions() {
        String curr_set_id = setList.get(selected_set_index).getId();
        String curr_cat_id = catList.get(selected_cat_index).getId();
        String curr_sub_id = level_list.get(selected_money_index).getId();

        quesList.clear();
        loadingDialog.show();

        firestore.collection("DETOUR").document(curr_set_id)
                .collection("CAT").document(curr_cat_id)
                .collection("SubCat").document(curr_sub_id)
                .collection("QUESTION").document("qList")
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()){
                                String quesName = (String) documentSnapshot.get("COUNT");
                                for (int i = 0; i <= Integer.valueOf(quesName);i++){
                                    String quesID = documentSnapshot.getString("Q" +String.valueOf(i) + "_ID");

                                    quesList.add(new QuestionModel(
                                            quesID,
                                            documentSnapshot.getString("QUESTION"),
                                            documentSnapshot.getString("A"),
                                            documentSnapshot.getString("B"),
                                            documentSnapshot.getString("C"),
                                            Integer.valueOf(documentSnapshot.getString("ANSWER"))

                                    ));

                                    if (i==1){
                                        addQB.setVisibility(View.GONE);
                                    }
                                }
                                questionAdapter = new QuestionAdapter(quesList);
                                quesView.setAdapter(questionAdapter);

                                loadingDialog.dismiss();
                            }
                            else {
                                Toast.makeText(QuestionListActivity.this, "No Questions here!!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(QuestionListActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(QuestionListActivity.this, "Not working", Toast.LENGTH_SHORT).show();
                        }
                        loadingDialog.dismiss();
                    }
                });
    }
}