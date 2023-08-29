package iplacex.evaluacion2.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Productos(
    @PrimaryKey(autoGenerate = true) val id:Int,
    var nombre:String,
    var comprado:Boolean
)