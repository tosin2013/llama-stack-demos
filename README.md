# Llama Stack on OCP


## Deploy
A `kustomization.yaml` file exists to launch all required Kubernetes objects for the scenarios defined in the repository. To create run the following.

```
oc create project llama-serve
oc apply -k kustomization.yaml
```