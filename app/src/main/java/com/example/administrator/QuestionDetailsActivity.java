package com.example.administrator;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArrayMap;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.administrator.CategoryActivity.catList;
import static com.example.administrator.CategoryActivity.selected_cat_index;
import static com.example.administrator.QuestionListActivity.quesList;
import static com.example.administrator.SetsActivity.selected_set_index;
import static com.example.administrator.SetsActivity.setList;
import static com.example.administrator.SubCategoryActivity.level_list;
import static com.example.administrator.SubCategoryActivity.selected_money_index;

public class QuestionDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.question)
    EditText ques;
    @BindView(R.id.optionA)
    EditText optionA;
    @BindView(R.id.optionB)
    EditText optionB;
    @BindView(R.id.optionC)
    EditText optionC;
    @BindView(R.id.answer)
    EditText answer;
    @BindView(R.id.addQB)
    Button addQB;

    private String qStr,aStr,bStr,cStr,ansStr;
    private Dialog loadingDialog;
    private FirebaseFirestore firestore;
    private String action;
    private int qID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_details);
        ButterKnife.bind(this);
        addQB.setOnClickListener(this);

        loadingDialog = new Dialog(QuestionDetailsActivity.this);
        loadingDialog.setContentView(R.layout.loading_progressbar);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        firestore = FirebaseFirestore.getInstance();
        action = getIntent().getStringExtra("ACTION");

        if (action.compareTo("EDIT") == 0)
        {

            qID = getIntent().getIntExtra("Q_ID",0);
//            getSupportActionBar().setTitle("QUESTION " +String.valueOf(qID + 1));
            loadData(qID);

        }
        else {
//            getSupportActionBar().setTitle("QUESTION " +  String.valueOf(quesList.size() + 1));
            addQB.setText("ADD");
        }
    }



    @Override
    public void onClick(View view) {
        if (view == addQB){
            qStr = ques.getText().toString();
            aStr = optionA.getText().toString();
            bStr = optionB.getText().toString();
            cStr = optionC.getText().toString();
            ansStr = answer.getText().toString();

            if (qStr.isEmpty()){
                ques.setError("Enter question");
                return;
            }

            if (aStr.isEmpty()){
                optionA.setError("Enter question");
                return;
            }
            if (bStr.isEmpty()){
                optionB.setError("Enter question");
                return;
            }
            if (cStr.isEmpty()){
                optionC.setError("Enter question");
                return;
            }
            if (ansStr.isEmpty()){
                answer.setError("Enter question");
                return;
            }

            if (action.compareTo("EDIT") == 0){
                editQuestion();
            }else {
                addNewQuestion();
            }
        }
    }







    private void addNewQuestion() {
        loadingDialog.show();


        String curr_set_id = setList.get(selected_set_index).getId();
        String curr_cat_id = catList.get(selected_cat_index).getId();
        String curr_sub_id = level_list.get(selected_money_index).getId();
        Map<String,Object> quesData = new ArrayMap<>();


        quesData.put("QUESTION",qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("ANSWER",ansStr);



        String set_id = firestore.collection("DETOUR").document().getId();
        String cate_id = firestore.collection("DETOUR").document(set_id).collection("CAT").document().getId();
        String M_id = firestore.collection("DETOUR").document(set_id).collection("CAT").document(cate_id).collection("SubCat").document().getId();
        String doc_id = firestore.collection("DETOUR").document(set_id).collection("CAT").document(cate_id)
                .collection("SubCat").document(M_id).collection("QUESTION").document().getId();

        firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document(curr_cat_id)
                .collection("SubCat").document(curr_sub_id).collection("QUESTION").document(doc_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Map<String,Object> quesDoc = new ArrayMap<>();
                        quesDoc.put("Q" + String.valueOf(quesList.size() + 1) + "_ID",doc_id);
                        quesDoc.put("COUNT",String.valueOf(quesList.size() + 1));

                        firestore.collection("DETOUR").document(curr_set_id).collection("CAT")
                                .document(curr_cat_id).collection("SubCat").document(curr_sub_id).collection("QUESTION")
                                .document("qList")
                                .update(quesDoc)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(QuestionDetailsActivity.this, "Question Added Successfully", Toast.LENGTH_SHORT).show();

                                        quesList.add(new QuestionModel(
                                                doc_id,
                                                qStr,aStr,bStr,cStr,ansStr
                                        ));

                                        loadingDialog.dismiss();
                                        QuestionDetailsActivity.this.finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        loadingDialog.dismiss();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                loadingDialog.dismiss();
            }
        });

    }


    private void loadData(int qID) {

        ques.setText(quesList.get(qID).getQuestion());
        optionA.setText(quesList.get(qID).getOptionA());
        optionB.setText(quesList.get(qID).getOptionB());
        optionC.setText(quesList.get(qID).getOptionC());
        answer.setText(quesList.get(qID).getCorrectAns());
    }

    private void editQuestion() {
        String curr_set_id = setList.get(selected_set_index).getId();
        String curr_cat_id = catList.get(selected_cat_index).getId();
        String curr_sub_id = level_list.get(selected_money_index).getId();
        String curr_ques_id = quesList.get(qID).getQuesID();
        loadingDialog.show();
        Map<String,Object> quesData = new ArrayMap<>();
        quesData.put("QUESTION",qStr);
        quesData.put("A",aStr);
        quesData.put("B",bStr);
        quesData.put("C",cStr);
        quesData.put("ANSWER",ansStr);



        firestore.collection("DETOUR").document(curr_set_id).collection("CAT")
                .document(curr_cat_id).collection("SubCat").document(curr_sub_id)
                .collection("QUESTION").document(curr_ques_id)
                .set(quesData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(QuestionDetailsActivity.this, "Question updated Successfully", Toast.LENGTH_SHORT).show();

                        quesList.get(qID).setQuestion(qStr);
                        quesList.get(qID).setOptionA(aStr);
                        quesList.get(qID).setOptionB(bStr);
                        quesList.get(qID).setOptionC(cStr);
                        quesList.get(qID).setCorrectAns(cStr);

                        loadingDialog.dismiss();
                        QuestionDetailsActivity.this.finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(QuestionDetailsActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        loadingDialog.dismiss();
                    }
                });
    }

}