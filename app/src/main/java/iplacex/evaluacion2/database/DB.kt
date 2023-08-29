package iplacex.evaluacion2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Productos::class], version = 1)
abstract class ProductosDB : RoomDatabase() {
    abstract fun productosDao(): ProductosDao

    companion object {
        @Volatile
        private var DATABASE : ProductosDB? = null
        fun getInstance(contexto: Context):ProductosDB {
            return DATABASE ?: synchronized(this) {
                Room.databaseBuilder(
                    contexto.applicationContext,
                    ProductosDB::class.java,
                    "Productos.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { DATABASE = it }
            }
        }
    }
}