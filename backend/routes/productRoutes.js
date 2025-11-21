import express from "express";
import multer from "multer";
import {
  getAllProducts,
  getProductById,
  createProduct,
  uploadProductImages,
  getProductImages,
  getAllCategories,
  getProductsByCategory,
} from "../repositories/productRepository.js";

const router = express.Router();
const upload = multer({ storage: multer.memoryStorage() });

router.get("/", async (req, res) => {
  try {
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 100;
    const products = await getAllProducts(page, limit);
    res.json(products);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al obtener productos: " + error.message });
  }
});

// --- Endpoint: obtener categorías únicas ---
router.get("/categorias", async (req, res) => {
  try {
    const categories = await getAllCategories();
    res.json({ ok: true, categories });
  } catch (error) {
    res.status(500).json({
      ok: false,
      error: "Error al obtener categorías: " + error.message,
    });
  }
});

// --- Endpoint: obtener productos por categoría ---
router.get("/categoria/:id/productos", async (req, res) => {
  try {
    const { id } = req.params;
    if (!/^\d+$/.test(id)) {
      return res.status(400).json({ error: "ID de categoría inválido" });
    }
    const page = parseInt(req.query.page) || 1;
    const limit = parseInt(req.query.limit) || 100;
    const products = await getProductsByCategory(parseInt(id), page, limit);
    res.json(products);
  } catch (error) {
    res.status(500).json({
      error: "Error al obtener productos por categoría: " + error.message,
    });
  }
});

router.get("/:id", async (req, res) => {
  try {
    const { id } = req.params;
    // Validar que el id sea numérico para evitar que rutas como '/categorias' queden capturadas aquí
    if (!/^\d+$/.test(id)) {
      return res.status(400).json({ error: "ID inválido" });
    }
    const product = await getProductById(id);
    if (!product) {
      return res.status(404).json({ error: "Producto no encontrado" });
    }
    res.json(product);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al obtener el producto: " + error.message });
  }
});

router.post("/", async (req, res) => {
  try {
    const newProductData = req.body;
    // Log detallado del body recibido para facilitar debugging cuando hay HTTP 500
    console.log(
      "POST /productos - body recibido:",
      JSON.stringify(newProductData)
    );
    const { nombre, precio, descripcion, id_usuario, stock, categoria } =
      newProductData;

    // Validaciones básicas y prevención de overflow numérico
    // Precio: numeric(10,2) en la BD -> el valor absoluto debe ser menor que 10^8
    const MAX_PRICE = 1e8; // 100,000,000

    // Comprobamos que los campos obligatorios existan
    if (
      nombre == null ||
      precio == null ||
      descripcion == null ||
      id_usuario == null ||
      stock == null ||
      categoria == null
    ) {
      return res.status(400).json({
        error:
          "Datos incompletos. Se requieren: nombre, precio, descripcion, id_usuario, stock, categoria.",
      });
    }

    // Asegurarnos de que precio sea numérico
    const precioNum = Number(precio);
    if (!isFinite(precioNum)) {
      return res.status(400).json({ error: "Precio inválido (no numérico)" });
    }

    // Validación de rango para evitar overflow en la columna numeric(10,2)
    if (Math.abs(precioNum) >= MAX_PRICE) {
      return res.status(400).json({
        error: `Precio fuera de rango. Debe ser menor que ${MAX_PRICE}`,
        received: precioNum,
      });
    }
    newProductData.fecha_publicacion = new Date().toISOString();

    const createdProduct = await createProduct(newProductData);
    res.status(201).json(createdProduct);
  } catch (error) {
    console.error(error);
    res
      .status(500)
      .json({ error: "Error al crear el producto: " + error.message });
  }
});

// --- Nuevo endpoint: obtener categorías únicas ---
// (moved earlier to avoid conflict with '/:id')

router.post("/:id/imagenes", upload.array("imagenes", 10), async (req, res) => {
  try {
    const id_producto = req.params.id;
    const files = req.files;

    if (!files || files.length === 0) {
      return res.status(400).json({ error: "No se enviaron archivos." });
    }

    const urls = await uploadProductImages(id_producto, files);

    res.status(201).json({
      message: "Imágenes subidas exitosamente",
      urls: urls,
    });
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al subir imágenes: " + error.message });
  }
});

router.get("/:id/imagenes", async (req, res) => {
  try {
    const { id } = req.params;
    const images = await getProductImages(id);

    res.json(images || []);
  } catch (error) {
    res
      .status(500)
      .json({ error: "Error al obtener las imágenes: " + error.message });
  }
});

export default router;
