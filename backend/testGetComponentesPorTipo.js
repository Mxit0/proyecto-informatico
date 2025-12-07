import fetch from "node-fetch";

const tipo = "Procesador"; // Cambia por gpu, motherboard, ram, etc.

const run = async () => {
  try {
    const res = await fetch(`http://localhost:3000/productos/tipos/${tipo}`);
    const data = await res.json();

    console.log(`Componentes del tipo "${tipo}":`);
    console.log(data);

  } catch (err) {
    console.error("Error:", err);
  }
};

run();