package com.example.administrator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.collection.ArrayMap;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import static com.example.administrator.CategoryActivity.catList;
import static com.example.administrator.CategoryActivity.selected_cat_index;
import static com.example.administrator.SetsActivity.selected_set_index;
import static com.example.administrator.SetsActivity.setList;
import static java.lang.Math.toIntExact;

public class SubCatAdapter extends RecyclerView.Adapter<SubCatAdapter.ViewHolder> {
    private List<SubCatModel> lev_list;


    public SubCatAdapter(List<SubCatModel> lev_list) {
        this.lev_list = lev_list;
    }

    @NonNull
    @Override
    public SubCatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout,parent,false);
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onBindViewHolder(@NonNull SubCatAdapter.ViewHolder holder, int position) {
        long Lev = lev_list.get(position).getName();
        holder.setLev(Lev,position,this);

    }

    @Override
    public int getItemCount() {
        return lev_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView setLevel;
        private ImageView deleteB;

        private Dialog loadingDialog;
        private Dialog editDialog;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setLevel = itemView.findViewById(R.id.setName);
            deleteB = itemView.findViewById(R.id.setDelB);


            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        public void setLev(long lev, int position, SubCatAdapter subCatAdapter) {
            setLevel.setText(String.valueOf(lev));
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SubCategoryActivity.selected_money_index = position;
                    Intent intent = new Intent(itemView.getContext(),QuestionListActivity.class);
                    itemView.getContext().startActivity(intent);
                }
            });
            deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog dialog = new AlertDialog.Builder(itemView.getContext())
                            .setTitle("Delete level")
                            .setMessage("Do you want to delete this level?")
                            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    deleteCategory(position,itemView.getContext(),subCatAdapter);
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

        private void deleteCategory(int position, Context context, SubCatAdapter subCatAdapter) {

            loadingDialog.show();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            String curr_set_id = setList.get(selected_set_index).getId();
            String curr_cat_id = catList.get(selected_cat_index).getId();

            Map<String,Object> subDoc = new ArrayMap<>();
            int index = 1;
            for(int i = 0;i < lev_list.size();i++){
                if (i != position){
                    subDoc.put("M" + String.valueOf(index) + "_ID",lev_list.get(i).getId());
                    subDoc.put("M" + String.valueOf(index) + "_NAME",lev_list.get(i).getName());
                    index++;
                }

            }
            subDoc.put("COUNT2",index-1);
            firestore.collection("DETOUR").document(curr_set_id).collection("CAT").document(curr_cat_id).collection("SubCat").document("M_List")
                    .set(subDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show();
                            SubCategoryActivity.level_list.remove(position);
                            subCatAdapter.notifyDataSetChanged();
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
