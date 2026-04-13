# User Stories

## User Management

- [x] As a customer, I want to register a new account, so that I can buy things online on the website

- [x] As A customer, I want to have a profile, so that I can store my personal information
    - Priority: Must have
    - A profile is automatically created after registration
    - Profile includes basic fields

- [x] As a customer, I want to update my profile, so that I can keep my information up-to-day

- [] As a customer, I want to receive a well-come email after registration
    - Priority: should have

- [] As a customer, I want to change my avatar, so that I can personalize my profile
    - Priority: Must have
    - Users can upload an image to update their avatar. The server generates two additional versions of the image: a
      medium-sized version and a thumbnail version.

- [] As a system, I want to check that customer addresses are valid in VietNam, so that the data is reliable
    - Priority: should have
    - Address must include fields province, ward, detailed_address
    - Addresses must be valid in Vietnam

- [] As a system, I want to detect whether a name looks Vietnamese, so that I can warn users and reduce invalid, random,
  and typo names
    - Priority: Should have
    - provide an api endpoint: `/api/v1/tools/validate/vietname-names`
    - Intput: `fullname` Output: `confidence sore` from 0 to 100

## Admin management

- [] As an admin, I want to have a store table, so that I can store the store information like owner, address, email,
  phone
- [] As a user, I want to make a request to be an admin, so that I can support the admin manage the store

## Communication

- As a customer, I want to receive notifications about my account activity so that I can monitor it
- As a customer, I want to chat with the owner, so that I can ask questions about products

## Product

- As an admin, I want to add products
- As an admin, I want to update product information
- As an admin, I want to mark a product as discontinued, so that it is no longer available for sale.
  As a customer, I want to view a list of products, so that I can browse available items.

- CRUD
- Search
- Category
- Rating
- Comment

## Discount

## Inventory

## Cart

## Order

## Payment

- COD
- payos

## Refund

## Delivery

## Content management