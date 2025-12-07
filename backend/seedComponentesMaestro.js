import { supabase } from "./lib/supabaseClient.js";

const componentes = [
  // ---------- CPUs ----------
  {
    nombre_componente: "Intel Core i7-12700",
    categoria: "cpu",
    especificaciones: {
      socket: "LGA1700",
      nucleos: 12,
      hilos: 20,
      frecuencia_base: "2.1 GHz"
    }
  },
  {
    nombre_componente: "AMD Ryzen 5 5600X",
    categoria: "cpu",
    especificaciones: {
      socket: "AM4",
      nucleos: 6,
      hilos: 12,
      frecuencia_base: "3.7 GHz"
    }
  },

  // ---------- GPUs ----------
  {
    nombre_componente: "NVIDIA GeForce RTX 2060",
    categoria: "gpu",
    especificaciones: {
      vram: "6GB GDDR6",
      tdp: 160
    }
  },
  {
    nombre_componente: "AMD Radeon RX 6600",
    categoria: "gpu",
    especificaciones: {
      vram: "8GB GDDR6",
      tdp: 132
    }
  },

  // ---------- Motherboards ----------
  {
    nombre_componente: "ASUS TUF Gaming B660M-PLUS",
    categoria: "motherboard",
    especificaciones: {
      socket: "LGA1700",
      formato: "Micro-ATX",
      memoria: "DDR4"
    }
  },
  {
    nombre_componente: "MSI B550-A PRO",
    categoria: "motherboard",
    especificaciones: {
      socket: "AM4",
      formato: "ATX",
      memoria: "DDR4"
    }
  },

  // ---------- RAM ----------
  {
    nombre_componente: "Corsair Vengeance 16GB DDR4 3200MHz",
    categoria: "ram",
    especificaciones: {
      tipo: "DDR4",
      velocidad: "3200MHz"
    }
  },
  {
    nombre_componente: "G.Skill Trident Z 16GB DDR5 6000MHz",
    categoria: "ram",
    especificaciones: {
      tipo: "DDR5",
      velocidad: "6000MHz"
    }
  }
];

const runSeeder = async () => {
  console.log("Insertando componentes en componente_maestro...");

  const { data, error } = await supabase
    .from("componente_maestro")
    .insert(componentes)
    .select();

  if (error) {
    console.error("Error al insertar:", error);
  } else {
    console.log("Componentes agregados con éxito:");
    console.log(data);
  }
};

runSeeder();