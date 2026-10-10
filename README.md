# Java Terminal Image Renderer
A light-weight system level image renderer using Java

<p align="center">
  <img width="1440" height="720" alt="Image" src="https://github.com/user-attachments/assets/d2eba0f6-3511-428a-82a3-7d5e8d0cc0f3" />
</p>

### Performance Benchmarks
Benchmarks were conducted using `hyperfine` with 5 warmup runs across different output resolution widths.

#### Results Overview
| Output Width | Execution Time (Mean ± $\sigma$) | Range (Min … Max) | User Time | System Time | Relative Speed |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **64 px** | 272.8 ms ± 5.8 ms | 265.3 ms … 283.0 ms | 426.8 ms | 103.8 ms | **1.00x** (Fastest) |
| **128 px** | 281.5 ms ± 6.5 ms | 273.0 ms … 292.6 ms | 451.0 ms | 107.6 ms | **1.03x** slower |
| **256 px** | 289.7 ms ± 5.5 ms | 284.1 ms … 299.7 ms | 472.3 ms | 110.8 ms | **1.06x** slower |
| **512 px** | 306.0 ms ± 4.1 ms | 301.3 ms … 314.1 ms | 526.4 ms | 112.6 ms | **1.12x** slower |
| **1024 px** | 347.9 ms ± 7.4 ms | 341.4 ms … 363.1 ms | 599.8 ms | 120.9 ms | **1.28x** slower |

#### Key Takeaways
- **JVM Startup Floor:** Initial JVM boot and class loading account for roughly **250–260 ms** of static baseline time.
- **Scaling Efficiency:** Increasing rendering resolution from 64 px to 1024 px (a $256\times$ increase in pixel density) results in only a **~27.5% increase** in total execution time.

### Benchmark Command

```bash
hyperfine --warmup 5 \
  'java -cp bin Main assets/SABA.png 64 > /dev/null' \
  'java -cp bin Main assets/SABA.png 128 > /dev/null' \
  'java -cp bin Main assets/SABA.png 256 > /dev/null' \
  'java -cp bin Main assets/SABA.png 512 > /dev/null' \
  'java -cp bin Main assets/SABA.png 1024 > /dev/null'
```
<details>
<summary><b> Hardware & System Specifications</b> (Click to expand)</summary>

<br>

| Component | Specification |
| :--- | :--- |
| **OS** | Fedora 44 (KDE Plasma) |
| **CPU** |  AMD Ryzen 5 7520U with Radeon Graphics |
| **RAM** | 8 GB LPDDR5 |
| **Storage** | 512 GB NVMe SSD |
| **Terminal** | Konsole |

</details>

## Extras
The entirety of Bad Apple!! played inside console. Render to .ansi fully using Java Terminal Image Renderer.
<p align="center">
  <img width="800" height="657" alt="Image" src="https://github.com/user-attachments/assets/043257ee-6a79-43e3-a59a-60f2d4c97c20" />
</p>


## Quick Start

### Prerequisites

- Java 11 or higher
- An ANSI-compliant terminal emulator (e.g., Konsole, Alacritty, Kitty, WezTerm)

### Compilation & Running

1. **Clone the repository:**
    ```bash
    git clone https://github.com/Okkoro-Dev/Java-Terminal-Image-Renderer.git
    cd Java-Terminal-Image-Renderer
    ```
2. **Compile the program:**
    ```bash
    mkdir -p bin
    javac -d bin src/Main.java
    ```
3. **Render the Image:**
    ```bash
    java -cp bin Main <path/to/image.png> [width] [height]
    ```
4. **Save output as an ANSI file (Optional):**
    ```bash
    java -cp bin Main <path/to/image.png> [width] [height] > output.ansi
    cat output.ansi
    ```
## License

Distributed under the MIT License. See `LICENSE` for details.