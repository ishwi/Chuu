import React from 'react';
import { Form, Formik, FormikHelpers } from 'formik';
import { IRegisterIn } from '../types/auth.types';
import { fetchPost } from '../services/Fetch.service';

export default class Register {
  render() {
    return (
      <main>
        <Formik
          initialValues={{
					  firstName: '',
					  lastName: '',
					  email: '',
					  password: '',
          }}
          onSubmit={(
					  values: IRegisterIn,
					  { setSubmitting }: FormikHelpers<IRegisterIn>,
          ) => {
					  fetchPost(
					    '/auth/register',
					    {
					      firstName: values.firstName,
					      lastName: values.lastName,
					      email: values.email,
					      password: values.password,
					    },
					  )
              .then((res) => {
					      //  Show success message
					    })
              .catch();
          }}
          render={() => <Form>{/* Form fields */}</Form>}
        />
      </main>
    );
  }
}
