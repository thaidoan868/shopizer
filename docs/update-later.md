## Update

- [ ] shopizer-user-service/src/main/java/vn/io/oldmoon/shopizer/user/infra/model/User.java
  @NotBlank @Builder.Default private String realm = "shopizer";
  should reference an environment variable
- authentication. lacking syncing for keycloak email and verification status updates.
- Keycloak set up is not ready for production