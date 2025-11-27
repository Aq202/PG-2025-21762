import yup from "yup";

export default yup.object().shape({
    name: yup.string().required("El campo 'name' es obligatorio."),
});
