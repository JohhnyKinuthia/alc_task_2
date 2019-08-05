package com.example.travelmantics;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DealAdapter extends RecyclerView.Adapter<DealAdapter.DealViewHolder> {
    ArrayList<TravelDeal> deals;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ChildEventListener mChildListener;
    private ImageView imageView;
    public DealAdapter(){
        mFirebaseDatabase = FirebaseUtil.mFirebaseDatabase;
        mDatabaseReference = FirebaseUtil.mDatabaseReference;
        deals = FirebaseUtil.mDeals;
        mChildListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TravelDeal deal = dataSnapshot.getValue(TravelDeal.class);
                Log.d("Deal", deal.getTitle());
                deal.setId(dataSnapshot.getKey());
                deals.add(deal);
                notifyItemInserted(deals.size()-1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mDatabaseReference.addChildEventListener(mChildListener);
    }
    @NonNull
    @Override
    public DealViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        Context ctx = viewGroup.getContext();
        View itemView = LayoutInflater.from(ctx).inflate(R.layout.rv_row, viewGroup, false);
        return new DealViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull DealViewHolder dealViewHolder, int i) {
        TravelDeal deal = deals.get(i);
        dealViewHolder.bind(deal);
    }

    @Override
    public int getItemCount() {
        return deals.size();
    }

    public class DealViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView tvTitle;
        TextView tvDescription;
        TextView tvPrice;
        public DealViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            imageView = itemView.findViewById(R.id.imageDeal);
            itemView.setOnClickListener (this);

        }
        public void bind(TravelDeal deal){
            tvTitle.setText(deal.getTitle());
            tvDescription.setText(deal.getDescription());
            tvPrice.setText(deal.getPrice());
            viewImage(deal.getImageUrl());
        }

        @Override
        public void onClick(View v) {
            int posistion = getAdapterPosition();
            Log.d("Click", String.valueOf(posistion));
            TravelDeal deal = deals.get(posistion);
            Intent intent = new Intent(itemView.getContext(), MainActivity.class);
            intent.putExtra("Deal", deal);
            itemView.getContext().startActivity(intent);

        }
    }

    public void viewImage(String url){
        if(url!=null && url.isEmpty()==false){
            Picasso.get().load(url).resize(160, 160).centerCrop().into(imageView);
        }
    }
}
