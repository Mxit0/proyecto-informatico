import fetch from "node-fetch";

const body = {
  id_componente_maestro: "10000000-0000-0000-0000-000000000007", // UUID existente de componente_maestro
  precio: 350000,
  descripcion: "Producto en excelente estado, probado.",
  id_usuario: 4,  // Cambiar según tu tabla usuario
  stock: 1
};

const run = async () => {
  try {
    const res = await fetch("http://localhost:3000/productos", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const data = await res.json();
    console.log("Respuesta del backend:");
    console.log(data);

  } catch (err) {
    console.error("Error:", err);
  }
};

run();