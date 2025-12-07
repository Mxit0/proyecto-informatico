package com.example.marketelectronico.data.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val imageUrl: String,
    val status: String = "Nuevo",
    val sellerId: Int = 4,
    val sellerName: String = "Vendedor Anónimo",

    val sellerImageUrl: String? = null,

    val sellerRating: Double = 0.0,
    val sellerReviews: Int = 0,
    val description: String = "No hay descripción disponible.",
    val specifications: Map<String, String> = emptyMap()
)

data class ChatPreview(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: String
)

data class Message(
    val id: String,
    val text: String,
    var isSentByMe: Boolean,
    var senderId: String = ""
)

data class ForumThread(
    val id: String,
    val title: String,
    val author: String,
    val replies: Int
)

data class ForumReply(
    val id: String,
    val author: String,
    val content: String
)

// --- DATOS DE MUESTRA (RESTAURADOS) ---
// Dejamos todos los datos de muestra para que las Previews
// y las pantallas no migradas sigan funcionando.

val sampleProduct1 = Product(
    id = "1",
    name = "CPU Intel Core i7",
    price = 250.0,
    imageUrl = "https://placehold.co/300x300/2D3748/FFFFFF?text=CPU",
    status = "Usado - Como Nuevo",
    sellerName = "TechTrader",
    sellerRating = 4.8,
    sellerReviews = 120,
    description = "El Intel Core i7-10700K es un procesador de escritorio de alto rendimiento...",
    specifications = mapOf(
        "Núcleos" to "8",
        "Hilos" to "16",
        "Reloj Base" to "3.8 GHz",
        "Socket" to "LGA 1200"
    )
)
val sampleProduct2 = Product(id = "2", name = "GPU NVIDIA RTX 3080", price = 700.0, imageUrl = "...", status = "Usado - Buen Estado", sellerName = "GamerZ", sellerRating = 4.5, sellerReviews = 89)
val sampleProduct3 = Product(id = "3", name = "RAM 16GB DDR4", price = 80.0, imageUrl = "...", status = "Nuevo", sellerName = "PartsWorld", sellerRating = 4.9, sellerReviews = 512)
val sampleProduct4 = Product(id = "4", name = "SSD Samsung 980 Pro 1TB", price = 180.0, imageUrl = "...")
val sampleProduct5 = Product(id = "5", name = "Motherboard ASUS ROG", price = 200.0, imageUrl = "...")
val sampleProduct6 = Product(id = "6", name = "Power Supply 750W", price = 120.0, imageUrl = "...")
val sampleProduct7 = Product(id = "7", name = "Case NZXT H510", price = 70.0, imageUrl = "...")
val sampleProduct8 = Product(id = "8", name = "Cooler Master Hyper 212", price = 40.0, imageUrl = "...")
val sampleProduct9 = Product(id = "9", name = "Monitor 144Hz", price = 300.0, imageUrl = "...")

val sampleRecommendations = listOf(sampleProduct1, sampleProduct2, sampleProduct3)
val sampleNews = listOf(sampleProduct4, sampleProduct5, sampleProduct6)
val sampleOffers = listOf(sampleProduct7, sampleProduct8, sampleProduct9)

// --- ¡LISTA RESTAURADA! ---
// Esta lista es la que causaba todos los errores
val allSampleProducts = (sampleRecommendations + sampleNews + sampleOffers).distinctBy { it.id }


// --- El resto de tus datos de muestra ---
val sampleChats = listOf(
    ChatPreview("1", "GamerZ", "Sí, la RTX 3080 aún está disponible.", "10:30 AM"),
    ChatPreview("2", "PartsWorld", "Tu pedido de RAM ha sido enviado.", "Ayer"),
    ChatPreview("3", "TechTrader", "¡Gracias por tu compra!", "Ayer")
)
val sampleMessages = listOf(
    Message("1", "Hola, ¿sigue disponible la RTX 3080?", true),
    Message("2", "¡Hola! Sí, aún la tengo.", false),
    Message("3", "Genial, ¿aceptas 650?", true),
    Message("4", "Lo siento, el precio es fijo en 700.", false)
)

val sampleThreads = listOf(
    ForumThread("1", "¿Es la RTX 4060 un buen upgrade desde la 2060?", "GamerZ", 12),
    ForumThread("2", "Problemas con socket LGA 1200", "TechTrader", 5),
    ForumThread("3", "Mejor SSD M.2 calidad/precio 2025", "PartsWorld", 34)
)
val originalPost = ForumThread("1", "¿Es la RTX 4060 un buen upgrade desde la 2060?", "GamerZ", 12)
val postContent = "Estoy pensando en actualizar mi vieja 2060 y vi la 4060 a buen precio. ¿Vale la pena el salto o mejor ahorro para una 4070?"
val sampleReplies = listOf(
    ForumReply("1", "TechTrader", "Depende de tu monitor. Para 1080p, la 4060 es genial."),
    ForumReply("2", "User123", "Yo ahorraría para la 4070, mucho más futuro.")
)