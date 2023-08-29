package iplacex.evaluacion2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import iplacex.evaluacion2.database.Productos
import iplacex.evaluacion2.database.ProductosDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Main()
        }
    }
}

enum class Accion {
    LISTAR, CREAR, EDITAR
}

@Composable
fun Main() {
    val context = LocalContext.current
    val dao = remember { ProductosDB.getInstance(context).productosDao() }

    val (productos, setProductos) = remember { mutableStateOf(emptyList<Productos>()) }
    val (seleccion, setSeleccion) = remember { mutableStateOf<Productos?>(null) }
    val (accion, setAccion) = remember { mutableStateOf(Accion.LISTAR) }

    LaunchedEffect(productos) {
        withContext(Dispatchers.IO) {
            setProductos(dao.obtenerTodos())
            Log.v("EV2", "LaunchedEffect()")
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val onSave = {
        setProductos(dao.obtenerTodos())
        setAccion(Accion.LISTAR)
    }

    when (accion) {
        Accion.CREAR -> AgregarOEditarProducto(null, onSave, onBack = { setAccion(Accion.LISTAR) })
        Accion.EDITAR -> AgregarOEditarProducto(
            seleccion,
            onSave,
            onBack = { setAccion(Accion.LISTAR) })

        else -> ListadoProductos(
            productos,
            onAdd = { setAccion(Accion.CREAR) },
            onEdit = { productos ->
                setAccion(Accion.EDITAR)
                setSeleccion(productos)
            },
            onDelete = { producto ->
                coroutineScope.launch(Dispatchers.IO) {
                    dao.eliminar(producto)
                    setProductos(dao.obtenerTodos())
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgregarOEditarProducto(producto: Productos?, onSave: () -> Unit = {}, onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val (nombre, setNombre) = remember { mutableStateOf(producto?.nombre ?: "") }
    val comprado = remember { mutableStateOf(producto?.comprado ?: false) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val txt_volver = context.getString(R.string.volver)
    val txt_producto = context.getString(R.string.producto)
    val txt_comprado = context.getString(R.string.comprado)
    val txt_alerta_nombre = context.getString(R.string.alerta_nombre)
    val txt_agregar = context.getString(R.string.agregar)
    val txt_guardar = context.getString(R.string.guardar)
    val txt_eliminar = context.getString(R.string.eliminar)

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onBack() },
                icon = {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                },
                text = { Text((txt_volver)) }
            )
        }
    ) { paddingValues ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Icon(
                Icons.Filled.ShoppingCart,
                modifier = Modifier
                    .height(80.dp)
                    .width(80.dp),
                tint = Color.Gray,
                contentDescription = "ShoppingCart"
            )
            Spacer(modifier = Modifier.height(10.dp))
            TextField(
                value = nombre,
                onValueChange = { setNombre(it) },
                label = { Text((txt_producto)) }
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = comprado.value,
                    onCheckedChange = { comprado.value = it }
                )
                Text(text = (txt_comprado))
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    if (nombre.isNotEmpty()) {
                        coroutineScope.launch(Dispatchers.IO) {
                            val dao = ProductosDB.getInstance(context).productosDao()
                            val producto = Productos(producto?.id ?: 0, nombre, comprado.value)
                            if (producto.id > 0) {
                                dao.actualizar(producto)
                            } else {
                                dao.insertar(producto)
                            }
                            onSave()
                        }
                    } else {
                        coroutineScope.launch(Dispatchers.IO) {
                            snackbarHostState.showSnackbar((txt_alerta_nombre))
                        }
                    }
                },
            ) {
                var textoGuardar = (txt_agregar)
                if (producto?.id ?: 0 > 0) {
                    textoGuardar = (txt_guardar)
                }
                Text(textoGuardar)
            }
            if (producto?.id ?: 0 > 0) {
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            val dao = ProductosDB.getInstance(context).productosDao()
                            if (producto != null) {
                                dao.eliminar(producto)
                            }
                            onSave()
                        }
                    }
                ) {
                    Text(txt_eliminar)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListadoProductos(productos: List<Productos>, onAdd: () -> Unit = {}, onEdit: (p: Productos) -> Unit = {}, onDelete: (p: Productos) -> Unit = {}) {
    val context = LocalContext.current
    val txt_agregar = context.getString(R.string.agregar)
    val txt_no_productos = context.getString(R.string.no_productos)


    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAdd() },
                icon = {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Add"
                    )
                },
                text = { Text((txt_agregar)) }
            )
        }
    ) { contentPadding ->
        if (productos.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(productos) { producto ->
                    Producto(producto, onClick = { onEdit(producto) }) {
                        onDelete(producto)
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    (txt_no_productos),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                )
            }
        }
    }
}

@Composable
fun Producto(productos: Productos, onClick: () -> Unit = {}, onDelete: () -> Unit = {}) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Spacer(
                modifier = Modifier
                    .height(70.dp)
            )
            Icon(
                imageVector = if (productos.comprado) Icons.Filled.Done else Icons.Filled.ShoppingCart,
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp),
                contentDescription = "ShoppingCart or Done"
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column() {
                Text(productos.nombre, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.Filled.Delete,
                tint = Color.Red,
                modifier = Modifier
                    .height(30.dp)
                    .width(30.dp)
                    .clickable { onDelete() },
                contentDescription = "Delete"
            )
        }
    }
}
