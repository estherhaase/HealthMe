package com.example.android.healthme;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.healthme.DataModel.Leg;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

    private Context context;
    private List<Leg> allLegs;

    public CustomAdapter(Context context, List<Leg> allLegs){
        this.allLegs = allLegs;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_content, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String textOrigin = allLegs.get(position).getPoints().get(0).getName();
        String textDest = allLegs.get(position).getPoints().get(1).getName();
        String line = allLegs.get(position).getMode().getNumber();
        String dur = allLegs.get(position).getTimeMinute() + " mins";

        holder.textView.setText(textOrigin);
        holder.textView2.setText(textDest);
        holder.textView4.setText(dur);
        if(!line.equals("")){
            holder.textView3.setText(line);
        }else {
            holder.textView3.setText("Walk");
        }
        if(Integer.parseInt(allLegs.get(position).getMode().getCode()) == -1){
            Picasso.get().load(R.drawable.walking).into(holder.imageView);
        }else {
            String code = allLegs.get(position).getMode().getCode();
            String plat = allLegs.get(position).getPoints().get(0).getRef().getPlatform();

            switch (code) {
                case "4":
                    Picasso.get().load(R.drawable.tram).into(holder.imageView);
                    break;

                case "5":
                    Picasso.get().load(R.drawable.bus).into(holder.imageView);
                    break;

                case "2":

                    switch (plat) {
                        case "U6-R":
                            Picasso.get().load(R.drawable.wienu6).into(holder.imageView);
                            break;
                        case "U6-H":
                            Picasso.get().load(R.drawable.wienu6).into(holder.imageView);
                            break;
                        case "U1-R":
                            Picasso.get().load(R.drawable.wienu1).into(holder.imageView);
                            break;
                        case "U1-H":
                            Picasso.get().load(R.drawable.wienu1).into(holder.imageView);
                            break;
                        case "U2-R":
                            Picasso.get().load(R.drawable.wienu2).into(holder.imageView);
                            break;
                        case "U2-H":
                            Picasso.get().load(R.drawable.wienu2).into(holder.imageView);
                            break;
                        case "U3-R":
                            Picasso.get().load(R.drawable.wienu3).into(holder.imageView);
                            break;
                        case "U3-H":
                            Picasso.get().load(R.drawable.wienu3).into(holder.imageView);
                            break;
                        case "U4-R":
                            Picasso.get().load(R.drawable.wienu4).into(holder.imageView);
                            break;
                        case "U4-H":
                            Picasso.get().load(R.drawable.wienu4).into(holder.imageView);
                            break;


                    }
                    break;

                default:
                    Picasso.get().load(R.drawable.wienerlinienlogo).into(holder.imageView);
                    break;
            }
        }

    }

    @Override
    public int getItemCount() {

        return allLegs.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView textView, textView2, textView3, textView4;


        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_route);
            textView = itemView.findViewById(R.id.text_route);
            textView2 = itemView.findViewById(R.id.text_route_dest);
            textView3 = itemView.findViewById(R.id.text_line);
            textView4 = itemView.findViewById(R.id.duration);

        }
    }
}
