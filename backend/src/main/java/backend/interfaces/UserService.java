package backend.interfaces;

import backend.models.User;

public interface UserService {
    User login(String email, String password);

    boolean signup(String email, String password, String usertype);

    boolean changePassword(long id, String password);

    boolean delete(long id);

    User getUserByID(long id);

    long getID(String email);

    User loginOrSignupGoogle(String email);
}
