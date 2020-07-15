package com.screenovate.superdo

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

/**
 * Grocery
 * @author Gabriel Noam
 */
@Entity
data class Grocery(@PrimaryKey(autoGenerate = true) var id: Int? = 0,
              @ColumnInfo(name = "name")
              @SerializedName("name")
              var name: String,
              @ColumnInfo(name = "weight")
              @SerializedName("weight")
              var weight: String,
              @ColumnInfo(name = "bagColor")
              @SerializedName("bagColor")
              var bagColor: String)

fun Grocery.weightAsFloat() =
    weight.replace("kg","").toFloatOrZero()


