package ch.codecamel.model;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.util.ArrayList;
import java.util.List;

public class HazelcastCacheTest {

    private static EntityManagerFactory entityManagerFactory;
    private static EntityManager entityManager;

    @BeforeClass
    public static void initialize() {

        // setup entitymanager
        entityManagerFactory = Persistence.createEntityManagerFactory("ch.codecamel");
        entityManager = entityManagerFactory.createEntityManager();

        // create entity
        List<Employee> empList = new ArrayList<>();
        empList.add(new Employee("John"));
        empList.add(new Employee("Mary"));

        Company company = new Company("FooCompany");
        company.setEmployees(empList);

        // persist entity
        entityManager.getTransaction().begin();
        System.out.println("persist " + company.getName());
        entityManager.persist(company);
        entityManager.getTransaction().commit();
    }

    @AfterClass
    public static void destroy() {
        if(entityManagerFactory != null) {
           entityManagerFactory.close();
        }
    }

    @Test
    public void readFromSecondLevelCache() {

        // read entity
        entityManager.getTransaction().begin();
        long start = System.nanoTime();
        Company company = entityManager.find(Company.class, 1L);
        long duration = System.nanoTime() - start;
        System.out.println("found " + company.getName() + " in " + nanoToMiliSec(duration) + "ms");
        System.out.println(" --> " + company.getEmployees().get(0).getName()); // lazy load employee to cache entity
        entityManager.getTransaction().commit();

        // read entity from cache
        entityManager.getTransaction().begin();
        start = System.nanoTime();
        company = entityManager.find(Company.class, 1L);
        duration = System.nanoTime() - start;
        System.out.println("found " + company.getName() + " in " + nanoToMiliSec(duration) + "ms");
        entityManager.getTransaction().commit();

        // read entity from cache
        entityManager.getTransaction().begin();
        start = System.nanoTime();
        Employee employee = entityManager.find(Employee.class, 1L);
        duration = System.nanoTime() - start;
        System.out.println("found " + employee.getName() + " in " + nanoToMiliSec(duration) + "ms");
        entityManager.getTransaction().commit();
    }


    private static float nanoToMiliSec(long nanosec) {
        return (float) nanosec /1000000f;
    }

}
