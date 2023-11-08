package com.example.xmatenotes.ui;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.xmatenotes.logic.model.CardData;

import com.example.xmatenotes.R;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.CardViewHolder> {

    private List<CardData> cardDataList;

    public CardAdapter(List<CardData> cardDataList) {
        this.cardDataList = cardDataList;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false);
        return new CardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        final CardData cardData = cardDataList.get(position);
        holder.titleTextView.setText(cardData.getTitle());

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 点击卡片显示详细信息

                Toast.makeText(v.getContext(), "点击卡片：" + cardData.getTitle(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), DetailMessage.class);
                intent.putExtra("detailInfo",cardData.getContent());
                v.getContext().startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return cardDataList.size();
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView titleTextView;
        TextView contentTextView;

        CardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);

        }
    }
}
