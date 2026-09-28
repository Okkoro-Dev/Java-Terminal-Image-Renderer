# Java Terminal Image Renderer

A lightweight, systems-level terminal image renderer written in Java. Engineered for high performance using zero-allocation byte buffers, cache-friendly planar memory access, and bulk ANSI stream I/O.

![Demo](assets/demo.png)

## Highlights

- **Zero-Allocation Hot Loop:** Reuses a single 1D planar `byte[]` buffer to process ARGB channels, completely bypassing Garbage Collector overhead during execution.
- **Cache-Friendly Memory Layout:** Keeps pixel operations tightly packed in memory to maximize L1/L2 CPU hardware cache utilization.
- **Bulk Output Redirection:** Leverages a 64 KB `BufferedWriter` to minimize costly system calls (`syscalls`), rendering full images in a single frame flip.
- **Fast Header Inspection:** Validates image file headers and magic bytes via `ImageInputStream` in under 1 ms without decoding full pixel buffers into RAM.
- **Clean ANSI Transparency:** Guards against background color smearing and trailing semicolon bugs with dedicated alpha-channel branching.

## Features

- Half-block character (`▀`) dual-pixel stacking for true 1:1 cell aspect ratios.
- Support for PNG, and JPEG file formats.
- Full output pipeline compatible with standard terminal redirection (`> output.ansi`).
- Custom width adjustment via `[target-width]` (default: 80 char wide)

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
    javac Main.java
    ```
3. **Render the Image:**
    ```bash
    java Main <path/to/image.png> [target-width]
    ```
4. **Save output as an ANSI file (Optional):**
    ```bash
    java Main <path/to/image.png> [target-width] > output.ansi
    cat output.ansi
    ```