package com.example.skarwa.articlesearch.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.skarwa.articlesearch.R;
import com.example.skarwa.articlesearch.model.Article;

import java.util.ArrayList;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by skarwa on 9/20/17.
 * <p>
 * Adapter class which extends Recycler view with 2 layouts for list item
 */

public class ArticleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int HAS_IMAGE = 0, NO_IMAGE = 1;
    private Context mContext;
    private ArrayList<Article> mArticles;
    private OnArticleSelectedListener listener;

    public interface OnArticleSelectedListener {
        void onArticleSelected(Article article);
    }


    public ArticleAdapter(Context context, ArrayList<Article> articles) {
        this.mContext = context;
        this.mArticles = articles;

    }

    class ViewHolderWithImage extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTitle)
        TextView tvTitle;

        @BindView(R.id.ivThumbnail)
        ImageView ivThumbnail;

        @BindView(R.id.etNewsDeskCategory)
        EditText etNewsDeskCategory;

        View view;

        ViewHolderWithImage(View itemView) {
            super(itemView);
            view = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    class ViewHolderWithNoImage extends RecyclerView.ViewHolder {
        @BindView(R.id.tvTitle)
        TextView tvTitle;

        @BindView(R.id.etNewsDeskCategory)
        EditText etNewsDeskCategory;

        View view;

        ViewHolderWithNoImage(View itemView) {
            super(itemView);
            view = itemView;
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * This method creates different RecyclerView.ViewHolder objects based on the item view type.\
     *
     * @param viewGroup ViewGroup container for the item
     * @param viewType  type of view to be inflated
     * @return viewHolder to be inflated
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(getContext());

        System.out.print("viewType:" + viewType);
        switch (viewType) {
            case HAS_IMAGE:
                View v1 = inflater.inflate(R.layout.item_article_with_image, viewGroup, false);
                viewHolder = new ViewHolderWithImage(v1);
                break;
            case NO_IMAGE:
                View v2 = inflater.inflate(R.layout.item_article_no_image, viewGroup, false);
                viewHolder = new ViewHolderWithNoImage(v2);
                break;
            default:
                View v = inflater.inflate(R.layout.item_article_no_image, viewGroup, false);
                viewHolder = new ViewHolderWithNoImage(v);
                break;
        }
        return viewHolder;
    }

    /**
     * This method internally calls onBindViewHolder(ViewHolder, int) to update the
     * RecyclerView.ViewHolder contents with the item at the given position
     * and also sets up some private fields to be used by RecyclerView.
     *
     * @param viewHolder The type of RecyclerView.ViewHolder to populate
     * @param position   Item position in the viewgroup.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        switch (viewHolder.getItemViewType()) {
            case HAS_IMAGE:
                ViewHolderWithImage vh1 = (ViewHolderWithImage) viewHolder;
                configureViewHolderWithImage(vh1, position);
                break;
            case NO_IMAGE:
                ViewHolderWithNoImage vh2 = (ViewHolderWithNoImage) viewHolder;
                configureViewHolderWithNoImage(vh2, position);
                break;
            default:
                ViewHolderWithNoImage vh = (ViewHolderWithNoImage) viewHolder;
                configureViewHolderWithNoImage(vh, position);
                break;
        }
    }

    private void configureViewHolderWithImage(ViewHolderWithImage vh1, int position) {
        Article article = mArticles.get(position);
        if (article != null) {
            vh1.tvTitle.setText(article.getHeadline());
            String new_desk = article.getNewDesk();
            vh1.etNewsDeskCategory.setText(new_desk);
            vh1.etNewsDeskCategory.setBackgroundResource(getColor(new_desk));

            Glide.with(getContext()).load(article.getThumbnail()).fitCenter()
                    .into(vh1.ivThumbnail);

            vh1.view.setOnClickListener(v -> {
                listener.onArticleSelected(article);
            });
        }
    }

    private void configureViewHolderWithNoImage(ViewHolderWithNoImage vh2, int position) {
        Article article = mArticles.get(position);
        if (article != null) {
            String new_desk = article.getNewDesk();
            vh2.tvTitle.setText(article.getHeadline());
            vh2.etNewsDeskCategory.setText(new_desk);
            vh2.etNewsDeskCategory.setBackgroundResource(getColor(new_desk));
            vh2.view.setOnClickListener(v -> {
                listener.onArticleSelected(article);
            });
        }
    }

    private int getColor(String newDesk) {
        if(newDesk.equalsIgnoreCase("Sports")){
            return R.color.colorSports;
        } else if(newDesk.equalsIgnoreCase("Fashion & Style")){
            return R.color.colorFashionStyle;
        } else if (newDesk.equalsIgnoreCase("Arts")){
            return R.color.colorArts;
        } else {
            return R.color.colorDefault;
        }
    }
    public void setOnArticleClickListener(OnArticleSelectedListener listener) {
        this.listener = listener;
    }


    @Override
    public int getItemCount() {
        return mArticles.size();
    }

    //Returns the view type of the item at position for the purposes of view recycling.
    @Override
    public int getItemViewType(int position) {
        if(mArticles.get(position)!=null){
            return mArticles.get(position).getArticleType().ordinal();
        }
        return 0;
    }

    public void clear() {
        int size = getItemCount();
        if (!mArticles.isEmpty()) {
            mArticles.clear();
           notifyItemRangeRemoved(0,size);
        }
    }

    private Context getContext() {
        return mContext;
    }

}
