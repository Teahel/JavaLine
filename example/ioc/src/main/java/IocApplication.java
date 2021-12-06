import com.ioc.test.annotation.IocAutowired;
import com.ioc.test.annotation.IocComponent;
import com.ioc.test.core.AnnotationApplcationContext;
import com.ioc.test.demo.IocAutowriedPeople;
import com.ioc.test.demo.People;

@IocComponent
public class IocApplication {

    @IocAutowired
    private static IocAutowriedPeople iocAutowriedPeople;

    public static void main(String[] args) {
        AnnotationApplcationContext context = new AnnotationApplcationContext();
        context.init(IocApplication.class);
        iocAutowriedPeople.show();
    }

}
