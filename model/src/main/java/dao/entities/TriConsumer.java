package dao.entities;


import java.sql.SQLException;

@FunctionalInterface
public
interface TriConsumer<A, B, C> {


    void accept(A a, B b, C c) throws SQLException;
}
