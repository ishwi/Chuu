import React from "react"
import {Form, Formik, FormikHelpers} from "formik";
import {IRegisterIn} from "../types/auth.types";
import "../services/Fetch.service";
import FetchService from "../services/Fetch.service";

export default class Register {
    render() {
        return (
            <main>
                <Formik
                    initialValues={{
                        firstName: "",
                        lastName: "",
                        email: "",
                        password: "",
                    }}
                    onSubmit={(
                        values: IRegisterIn,
                        {setSubmitting}: FormikHelpers<IRegisterIn>
                    ) => {
                        FetchService.isofetch(
                            "/auth/register",
                            {
                                firstName: values.firstName,
                                lastName: values.lastName,
                                email: values.email,
                                password: values.password,
                            },
                            "POST"
                        )
                            .then(res => {
                                //  show success message
                            })
                            .catch()
                    }}
                    render={() => <Form>{/*form fields*/}</Form>}
                />
            </main>
        )
    }
}
