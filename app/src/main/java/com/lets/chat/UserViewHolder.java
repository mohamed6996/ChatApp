package com.lets.chat;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserViewHolder extends RecyclerView.ViewHolder {

    CircleImageView user_image;
    TextView user_name, user_status;

    public UserViewHolder(View itemView) {
        super(itemView);


        user_image = (CircleImageView) itemView.findViewById(R.id.user_image);
        user_name = (TextView) itemView.findViewById(R.id.user_display_name);
        user_status = (TextView) itemView.findViewById(R.id.user_status);

    }

    public void setName(String name) {
        user_name.setText(name);
    }

    public void setStatus(String status) {
        user_status.setText(status);
    }

    public void setImage(String image, Context context) {

        Picasso.with(context).load(image)
                .placeholder(R.drawable.noun_323186_cc)
                .into(user_image);
    }

}
