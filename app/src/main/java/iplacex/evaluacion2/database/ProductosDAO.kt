package iplacex.evaluacion2.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ProductosDao {
    @Query("SELECT * FROM productos ORDER BY comprado")
    fun obtenerTodos(): List<Productos>

    @Insert
    fun insertar(productos: Productos):Long

    @Update
    fun actualizar(productos: Productos)

    @Delete
    fun eliminar(productos: Productos)
}