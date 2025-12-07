import fetch from "node-fetch";

const run = async () => {
  try {
    const res = await fetch("http://localhost:3000/productos/tipos");
    const data = await res.json();

    console.log("Tipos de componentes:");
    console.log(data);

  } catch (err) {
    console.error("Error:", err);
  }
};

run();