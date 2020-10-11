package com.example.administrator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import static com.example.administrator.SetsActivity.selected_set_index;
import static com.example.administrator.SetsActivity.setList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<CategoryModel> cat_List;


    public CategoryAdapter(List<CategoryModel> cat_List) {
        this.cat_List = cat_List;
    }

    @NonNull
    @Override
    public CategoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.ViewHolder holder, int position) {
        String cat_Title = cat_List.get(position).getName();
        holder.setData(cat_Title,position,this);
    }

    @Override
    public int getItemCount() {
        return cat_List.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView catName;
        private ImageView deleteB;

        private Dialog loadingDialog;
        private Dialog editDialog;
        private EditText tv_editCatName;
        private Button updateCatB;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            catName = itemView.findViewById(R.id.setName);
            deleteB = itemView.findViewById(R.id.setDelB);


            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        }

        public void setData(String cat_title, int position, CategoryAdapter categoryAdapter) {
            catName.setText(cat_title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    CategoryActivity.selected_cat_index = position;
                    Intent intent = new Intent(itemView.getContext(),SubCategoryActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });

            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete Category")
                            .setMessage("Do you want to delete this category?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteCategory(position,itemView.getContext(),categoryAdapter);
                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    dialog.getButton(dialog.BUTTON_POSITIVE).setBackgroundColor(Color.GREEN);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setBackgroundColor(Color.GREEN);


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0,0,50,0);
                    dialog.getButton(dialog.BUTTON_NEGATIVE).setLayoutParams(params);
                }


            });
            }

        private void deleteCategory(int position, Context context, CategoryAdapter categoryAdapter) {

            loadingDialog.show();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String curr_set_id = setList.get(selected_set_index).getId();
            Map<String,Object> catDoc = new ArrayMap<>();
            int index =1;
            for (int i = 0;i < cat_List.size();i++){
                if (i != position){
                    catDoc.put("CAT" + String.valueOf(index) + "_ID",cat_List.get(i).getId());
                    catDoc.put("CAT" + String.valueOf(index) + "_NAME",cat_List.get(i).getName());
                    index++;
                }
            }
            catDoc.put("COUNT1",index-1);
            firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document("categories")
                    .set(catDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Category Deleted SuccessFully", Toast.LENGTH_SHORT).show();
                            CategoryActivity.catList.remove(position);
                            categoryAdapter.notifyDataSetChanged();
                            loadingDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });
        }


    }
}




































































