package core.services;

import core.Chuu;
import dao.entities.ScrobbledAlbum;
import dao.entities.TimestampWrapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

import java.util.Arrays;
import java.util.List;

@Aspect
public class RecentTracksInterceptor {

    @AfterReturning(pointcut = "execution(* core.apis.last.ConcurrentLastFM.*(..)))")
    public void beforeeAdvice(JoinPoint joinPoint) {
        System.out.println(Arrays.toString(joinPoint.getArgs()));
        //Chuu.getLogger().warn(username);
        //System.out.println(username);
        //Chuu.getLogger().warn("Found " + list.getWrapped().size());

    }
}
