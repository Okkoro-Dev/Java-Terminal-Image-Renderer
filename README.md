# Java Terminal Image Renderer

A lightweight, systems-level terminal image renderer written in Java. Engineered for high performance using zero-allocation byte buffers, cache-friendly planar memory access, and bulk ANSI stream I/O.

<p align="center">
  <img width="1440" height="720" alt="Image" src="https://github.com/user-attachments/assets/d2eba0f6-3511-428a-82a3-7d5e8d0cc0f3" />
</p>

## Highlights

- **Zero-Allocation Hot Loop:** Reuses a single 1D planar `byte[]` buffer to process ARGB channels, completely bypassing Garbage Collector overhead during execution.
- **Cache-Friendly Memory Layout:** Keeps pixel operations tightly packed in memory to maximize L1/L2 CPU hardware cache utilization.
- **Bulk Output Redirection:** Leverages a 64 KB `BufferedWriter` to minimize costly system calls (`syscalls`), rendering full images in a single frame flip.
- **Fast Header Inspection:** Validates image file headers and magic bytes via `ImageInputStream` in under 1 ms without decoding full pixel buffers into RAM.

## Features

- Half-block character (`▀`) dual-pixel stacking for true 1:1 cell aspect ratios.
- Support for PNG, and JPEG file formats.
- Full output pipeline compatible with standard terminal redirection (`> output.ansi`).
- Custom pixel dimensions adjustment via `[width] [height]` (default: 80 char wide)

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
