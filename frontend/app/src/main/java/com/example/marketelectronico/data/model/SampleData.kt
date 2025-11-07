package com.example.marketelectronico.data.model

// 1. Aquí está tu clase Product, CON TODOS LOS CAMPOS
// Damos valores por defecto para que no se rompa MainScreen al crear productos simples.
data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val status: String = "Nuevo",
    val sellerName: String = "Vendedor Anónimo",
    val sellerRating: Double = 0.0,
    val sellerReviews: Int = 0,
    val description: String = "No hay descripción disponible.",
    val specifications: Map<String, String> = emptyMap()
)

// 2. Datos de muestra con todos los campos llenos (para Recomendaciones)

val sampleProduct1 = Product(
    id = "1",
    name = "CPU Intel Core i7",
    price = 250.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=CPU",
    status = "Usado - Como Nuevo",
    sellerName = "TechTrader",
    sellerRating = 4.8,
    sellerReviews = 120,
    description = "El Intel Core i7-10700K es un procesador de escritorio de alto rendimiento con 8 núcleos y 16 hilos, ideal para juegos y creación de contenido.",
    specifications = mapOf(
        "Núcleos" to "8",
        "Hilos" to "16",
        "Reloj Base" to "3.8 GHz",
        "Reloj Boost" to "5.1 GHz",
        "Socket" to "LGA 1200"
    )
)

val sampleProduct2 = Product(
    id = "2",
    name = "GPU NVIDIA RTX 3080",
    price = 700.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=GPU",
    status = "Usado - Buen Estado",
    sellerName = "GamerZ",
    sellerRating = 4.5,
    sellerReviews = 89,
    description = "Potente tarjeta gráfica para juegos en 4K.",
    specifications = mapOf("Memoria" to "10GB GDDR6X", "Núcleos CUDA" to "8704")
)

val sampleProduct3 = Product(
    id = "3",
    name = "RAM 16GB DDR4",
    price = 80.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=RAM",
    status = "Nuevo",
    sellerName = "PartsWorld",
    sellerRating = 4.9,
    sellerReviews = 512,
    description = "Memoria RAM Corsair Vengeance 3200MHz.",
    specifications = mapOf("Capacidad" to "16GB (2x8GB)", "Velocidad" to "3200MHz")
)

// --- Productos para 'Novedades' (con campos por defecto) ---
val sampleProduct4 = Product(
    id = "4",
    name = "SSD Samsung 980 Pro 1TB",
    price = 180.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=SSD"
)

val sampleProduct5 = Product(
    id = "5",
    name = "Motherboard ASUS ROG",
    price = 200.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=Placa"
)

val sampleProduct6 = Product(
    id = "6",
    name = "Power Supply 750W",
    price = 120.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=Fuente"
)

// --- (INICIO) ACTUALIZACIÓN DE DATOS ---
// --- Productos para 'Ofertas' (con campos por defecto) ---
val sampleProduct7 = Product(
    id = "7",
    name = "Case NZXT H510",
    price = 70.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=Gabinete"
)
val sampleProduct8 = Product(
    id = "8",
    name = "Cooler Master Hyper 212",
    price = 40.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=Cooler"
)
val sampleProduct9 = Product(
    id = "9",
    name = "Monitor 144Hz",
    price = 300.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=Monitor"
)
// --- (FIN) ACTUALIZACIÓN DE DATOS ---


// 3. Listas que usarán tus pantallas
val sampleRecommendations = listOf(sampleProduct1, sampleProduct2, sampleProduct3)
val sampleNews = listOf(sampleProduct4, sampleProduct5, sampleProduct6)

// --- LISTA DE OFERTAS ACTUALIZADA ---
val sampleOffers = listOf(sampleProduct7, sampleProduct8, sampleProduct9)

// 4. Lista 'allSampleProducts' ACTUALIZADA para incluir las nuevas ofertas
//    (Se usa un Set para evitar duplicados si un producto está en varias listas)
val allSampleProducts = (sampleRecommendations + sampleNews + sampleOffers)
    .distinctBy { it.id }