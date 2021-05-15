import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.filters.FilterBasedOnInheritance;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import dao.entities.LbEntry;
import org.junit.Test;

import java.util.List;

public class Beans {

    // The package to test
    private static final String POJO_PACKAGE = "dao.entities";
    private static final String EXCEPTION_PACKAGE = "core.exceptions";

    @Test
    public void ensureExpectedPojoCount() {

        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClasses(POJO_PACKAGE,
                new FilterPackageInfo());
        pojoClasses.addAll(PojoClassFactory.getPojoClasses(EXCEPTION_PACKAGE, new FilterPackageInfo()));
        //Affirm.affirmEquals("Classes added / removed?", EXPECTED_CLASS_COUNT, pojoClasses.size());
    }

    @Test
    public void testPojoStructureAndBehavior() {
        Validator validator = ValidatorBuilder.create()
                // Add Rules to validate structure for POJO_PACKAGE
                // See com.openpojo.validation.rule.impl for more ...
                //		.with(new GetterMustExistRule())
                //		.with(new SetterMustExistRule())

                // Add Testers to validate behaviour for POJO_PACKAGE
                // See com.openpojo.validation.test.impl for more ...
                .with(new SetterTester())
//				.with(new )
                .with(new GetterTester())
                .build();

        validator.validate(POJO_PACKAGE, new FilterPackageInfo(), new FilterBasedOnInheritance(LbEntry.class));
    }
}
