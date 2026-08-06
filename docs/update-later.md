## Update

- [ ] shopizer-user-service/src/main/java/vn/io/oldmoon/shopizer/user/infra/model/User.java
  @NotBlank @Builder.Default private String realm = "shopizer";
  should reference an environment variable
- should add documentation to entities

## Notes

The application is designed to have only one role for every user