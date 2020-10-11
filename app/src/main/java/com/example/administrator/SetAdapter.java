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

public class SetAdapter extends RecyclerView.Adapter<SetAdapter.ViewHolder> {

    public List<SetModel> set_list;

    public SetAdapter(List<SetModel> set_list) {
        this.set_list = set_list;
    }

    @NonNull
    @Override
    public SetAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        view = LayoutInflater.from(parent.getContext()).inflate(R.layout.set_item_layout,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SetAdapter.ViewHolder holder, int position) {
        String title = set_list.get(position).getName();
        holder.setData(title,position,this);
    }

    @Override
    public int getItemCount() {
        return set_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView setName;
        private ImageView deleteB;

        private Dialog loadingDialog;
        private Dialog editDialog;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setName = itemView.findViewById(R.id.setName);
            deleteB = itemView.findViewById(R.id.setDelB);


            loadingDialog = new Dialog(itemView.getContext());
            loadingDialog.setContentView(R.layout.loading_progressbar);
            loadingDialog.setCancelable(false);
            loadingDialog.getWindow().setBackgroundDrawableResource(R.drawable.progress_background);
            loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setData(String title, int position, SetAdapter setAdapter) {

            setName.setText(title);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    SetsActivity.selected_set_index = position;
                    Intent intent = new Intent(itemView.getContext(),CategoryActivity.class);
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
                                    deleteSet(position,itemView.getContext(),setAdapter);
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

        private void deleteSet(final int position, Context context, SetAdapter setAdapter) {
            loadingDialog.show();
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();

            Map<String,Object> setDoc = new ArrayMap<>();
            int index = 1;
            for (int i = 0;i < set_list.size();i++){
                if (i != position){
                    setDoc.put("SET" + String.valueOf(index) + "_ID",set_list.get(i).getId());
                    setDoc.put("SET" + String.valueOf(index) + "_NAME",set_list.get(i).getName());
                    index++;
                }
            }
            setDoc.put("COUNT",index - 1);

            firestore.collection("DETOUR").document("SETS")
                    .set(setDoc)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "Set Deleted Successfully", Toast.LENGTH_SHORT).show();
                            SetsActivity.setList.remove(position);

                            setAdapter.notifyDataSetChanged();
                            loadingDialog.dismiss();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(context,e.getMessage(), Toast.LENGTH_SHORT).show();
                    loadingDialog.dismiss();
                }
            });
        }
    }
}
