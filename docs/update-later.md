## Update

- [ ] shopizer-user-service/src/main/java/vn/io/oldmoon/shopizer/user/infra/model/User.java
  @NotBlank @Builder.Default private String realm = "shopizer";
  should reference an environment variable
- /api/v1/customers/me/profile endpoint doesn't validate addresses and phone numbers
- Should send an email after a user is created

## agy commands

Clean up the code and fix errors
Don't redo what you just did. follow my new code structure instead.