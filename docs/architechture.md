# Old Shopizer Structure

The origin shopizer's structure is a mess. The first time I saw it, I was lost in the codebase.

Here is a brief overview of the old shopizer structure:

- **sm-core-model:**
    - /model
- **sm-core:**
    - /repositories
    - /services
- **sm-shop-model:**
    - /model // dto
- **sm-shop:**
    - /mappers
    - store/controller // facade interfaces
    - store/facades // facade implementations
    - store/apis // controllers
    - 