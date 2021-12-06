import com.ioc.test.annotation.IocComponentScan;
import com.ioc.test.core.AnnotationApplcationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@IocComponentScan("com.ioc.test")
public class IocApplication {

    public static void main(String[] args) {
        AnnotationApplcationContext context = new AnnotationApplcationContext();
        context.init(IocApplication.class);
    }

}
