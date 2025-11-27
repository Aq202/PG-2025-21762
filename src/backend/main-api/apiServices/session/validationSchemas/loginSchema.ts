import yup from 'yup';
export default yup.object().shape({
    password: yup.string().required("El campo 'password' es obligatorio."),
    email: yup
        .string()
        .required("El campo 'email' es obligatorio."),
});
