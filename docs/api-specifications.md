# API Specifications
## I. Objectives
Our customer want to build an e-ecommerce website for his cloth shop. The website has full features from showing clothes to payment and shipping.
# V0 Objectives
Build a solid foundation for the shop that can be easily updated.

- **Party APIs**: Manage users. Update customer profiles, change addresses, avatars, and preferred languages.
- **Store APIs**: Manage merchant stores, including ownership details, supported languages, and store information such as name, logo, etc.
- **Notification APIs**: Merchants receive notifications when customers place orders. Customers receive updates on their order statuses.
- **Product APIs**: Provide product information such as manufacturer, description, and images. Support product variations (e.g., sizes, colors) and inventory tracking for each variant.
- **Cart, Order, Payment, Shipping**: Handle product checkout, payment, and shipping to customers.
- **Search APIs**: Support product searches by name and description, with autocomplete functionality.
- **Content Management APIs**: Provide a simple CMS for the frontend to store and manage data.

# V1 Updates
Support the following additional features:

- Discounts and promotions
- Online payments


## II. APIS

### 1. Party APIS
####    1.1 Customer APIS
####    1.2 Admin Authentication APIS

### 2. Store APIS

### 3. Notification APIS
####    3.1 Customer Notification APIS
####    3.2 Customer Merchant APIS

### 3. Product APIS
Product
- List<Attribute>
    - Option
    - Option Value

Inventory
- Product
- Available

PropertySet
- Option
- List<option value>

Variation
- Option
- Option Value
####    3.1 Catalog APIS
####    3.2 Category APIS
####    3.3 Product Group APIS
####    3.4 Product APIS
Example Product
* **Name**: Nike Classic T-Shirt (Black, Size M)
* **Description**: 100% cotton, lightweight black t-shirt, size Medium
* **Price**: \$25.99 (Discount: \$19.99 valid until 2025-09-25)
* **Categories**: Apparel → T-Shirts
* **Availability**: In stock (120 units), min order 1, max order 5
* **Attributes**:
    * **Color** = Black
    * **Size** = M
* **Images**: product photo (front, back)
* **Specifications**:
    * **Weight**: 180g
    * **Dimensions**: 30 × 25 × 2 cm
* **Manufacturer**: Nike

####    3.5 Product Image APIS
####    3.6 Manufacturer APIS
####    3.7 Option and Attribute APIS
An attribute links the product to a specific option (e.g., Color) and an option value (e.g., Red).
- **GET** `/api/v1/private/product/{id}/attributes` → list all attributes of a product.

An option is a configurable field that can create variations of the product. For example, Size (S, M, L), Color (Red, Blue, Green)
- **POST** `/api/v1/private/product/option` → define an option (e.g., “Color”).

An option value is a specific value for an option. For example, for the option Color, the values can be Red, Blue, Green.
- **POST** `/api/v1/private/product/option/value` → define an option value (e.g., “Red”).
- **POST** `/api/v1/private/product/option/value/{id}/image` → link an image to the value (e.g., picture of red shirt).

Example for a T-shirt product:
- **Attribute**:
    - **price**: 20.00
    - **weight**: 0.5
    - **Option**:
        - **Type**: Color
    - **Option value**:
        - **Value**: Red

####    3.8 Product type APIS
A product type defines a category of products that share common attributes and options. For example, T-Shirts, Hoodies, Pants, etc.

A t-shirt type:
- **code**: tshirt
- **descriptions**: T-Shirt

####    3.9 Property set APIS
A property set is basically a bundle of product properties (an option + its values) that can be applied to one or more product types.

Example: You define a Property Set for T-Shirts:
- **Option**: Color
- **Option Values**: Red, Blue, Green
- **Product Types**: T-Shirt, Hoodie

####    3.10 Variation APIS
A variation is a specific combination of an option and option values for a product.
Example: For a T-shirt product:
- **code**: tshirt-red-m
- **option**: 1
- **option value**: 2

####    3.11 Inventory APIS
Let’s say you sell a Nike Classic T-Shirt:
* **Product ID**: 2001
* **Store**: amazon
* **Region**: US
* **Available**: Yes (from Sept 20, 2025)
* **Quantity in stock**: 500
* **Min order**: 1
* **Max order**: 5
* **Price**: \$25.99 (discount \$19.99 from Sept 20–25)

### 4. Cart APIS

### 5. Order APIS
####    5.1 Total Calculation APIS
####    5.1 Order Status APIS

### 6. Payment APIS

### 7. Shipping APIS

### 8. Tax APIS

### 9. Search APIS

### 10. Content Management APIS
####    6.1 Page APIS
####    6.2 Content & Box APIS
####    6.3 Folder APIS
####    6.4 File APIS