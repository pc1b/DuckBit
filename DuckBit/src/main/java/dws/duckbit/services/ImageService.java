package dws.duckbit.services;

import dws.duckbit.entities.UserD;
import dws.duckbit.repositories.ComboRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.hibernate.engine.jdbc.BlobProxy;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Optional;

import static org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentRequest;

@Service
public class ImageService {

    private final UserService userService;
    public ImageService(UserService userService) {
        this.userService = userService;
    }
    public ResponseEntity<Object> getImage(String username) throws SQLException {
        Optional<UserD> user = this.userService.findByUsername(username);
        if (user.isEmpty())
            return ResponseEntity.notFound().build();
        UserD u = user.get();
        if (u.getImageFile() != null)
        {

            Resource file = new InputStreamResource(u.getImageFile().getBinaryStream());
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .contentLength(u.getImageFile().length()).body(file);
        }
        else
            return ResponseEntity.notFound().build();
    }

    public ResponseEntity<Object> uploadImage(UserD user, @RequestParam MultipartFile imageFile)
    {
        URI location = fromCurrentRequest().build().toUri();
        Blob image;
        try
        {
            image = BlobProxy.generateProxy(imageFile.getInputStream(), imageFile.getSize());
        }
        catch (IOException e)
        {
            return ResponseEntity.badRequest().body("Not an image");
        }
        user.setImageFile(image);
        this.userService.save(user);
        return ResponseEntity.created(location).build();
    }
    public UserD deleteImage(HttpServletRequest request) throws IOException
    {
        UserD u = this.userService.findByUsername(request.getUserPrincipal().getName()).get();
        u.setImageFile(null);
        this.userService.save(u);
        return u;
    }
}
