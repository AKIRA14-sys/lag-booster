package com.lagbooster.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lagbooster.R
import com.lagbooster.manager.GameAppInfo

class GameAdapter(
    private var items: List<GameAppInfo>,
    private val onItemClick: (GameAppInfo) -> Unit,
    private val onFavoriteClick: (GameAppInfo) -> Unit
) : RecyclerView.Adapter<GameAdapter.GameViewHolder>() {

    class GameViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cardContainer: LinearLayout = view.findViewById(R.id.cardContainer)
        val tvFavoriteStar: TextView = view.findViewById(R.id.tvFavoriteStar)
        val ivHeart: ImageView = view.findViewById(R.id.ivHeart)
        val ivGameIcon: ImageView = view.findViewById(R.id.ivGameIcon)
        val tvGameTitle: TextView = view.findViewById(R.id.tvGameTitle)
        val tvGameStatus: TextView = view.findViewById(R.id.tvGameStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = items[position]

        holder.tvGameTitle.text = game.title.uppercase()
        if (game.icon != null) {
            holder.ivGameIcon.setImageDrawable(game.icon)
        } else {
            holder.ivGameIcon.setImageResource(R.drawable.ic_launcher_foreground)
        }

        if (game.isSelected) {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_card_selected)
            holder.tvGameStatus.text = "SELECTED"
            holder.tvGameStatus.setTextColor(Color.parseColor("#FF1744"))
        } else {
            holder.cardContainer.setBackgroundResource(R.drawable.bg_card_normal)
            holder.tvGameStatus.text = "INSTALLED"
            holder.tvGameStatus.setTextColor(Color.parseColor("#00E5FF"))
        }

        if (game.isFavorite) {
            holder.tvFavoriteStar.setTextColor(Color.parseColor("#FF1744"))
            holder.ivHeart.setColorFilter(Color.parseColor("#FF1744"))
        } else {
            holder.tvFavoriteStar.setTextColor(Color.parseColor("#8090A6"))
            holder.ivHeart.setColorFilter(Color.parseColor("#408090A6"))
        }

        holder.ivHeart.setOnClickListener {
            onFavoriteClick(game)
        }

        holder.itemView.setOnClickListener {
            onItemClick(game)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<GameAppInfo>) {
        items = newItems
        notifyDataSetChanged()
    }
}
