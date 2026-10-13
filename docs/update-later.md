## Update later

- /api/v1/customers/me/profile endpoint doesn't validate addresses and phone numbers
- Should send an email after a user is created
- /api/v1/users/me/avatar endpoint could be attacked with Decompression DoS and a lot of other attacks.
- update shopizer-user-service health endpoints
- Update keycloak relam-export.json. disable email verification

- handle PathVariable errors

## agy commands

Create Product: Implement a POST endpoint at /api/v1/product-service/products.
Implement a GET endpoint at /api/v1/product-service/products/{identifier} that retrieves a product using either its UUID
or its slug.

Create Product DTOs and mappers. Ensure mappers are utilized at the controller level so the underlying service layer
only interacts with domain entities.

Create ProductPopulator that uses ProductMapper to map dtos to domain entities. Add debug logs after mapping
Keep ProductMapper as simple as possible. The logs in populators should contain a message and an id is enough

Apply appropriate request validations. Add @Schema annotations to generate OpenAPI documentation, ensuring you document
only one happy path for one API. The app is for selling computer components, so use computer components as examples

Do not write any tests for these implementations yet.

Don't create classes that only has one line which call another class. Like two names for one function. One function has
only one name

Use Objects.requireNonNull(dto) to prevent functions from returning null values

Don't add unused methods.

## Future releases

5/9
product
compound products
sku
elasticsearch
paging
product view
Promotional Pricing & Flash Sales
Tiered & Volume Pricing
translation
inventory
CMS for FE_DEV
1/10

1/10
order
online sale
print bill
notification
promotion
comment, rating
Validate address, phone number
20/10

20/10
offline sale
Employee management
ke toan
bao hanh, sua chua

tax

chat
10/10

Stress tests
AWS
25/10

## Needed further research